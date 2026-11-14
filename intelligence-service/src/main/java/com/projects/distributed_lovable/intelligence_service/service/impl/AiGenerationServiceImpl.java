package com.projects.distributed_lovable.intelligence_service.service.impl;



import com.projects.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.projects.distributed_lovable.common_lib.enums.ChatEventType;
import com.projects.distributed_lovable.common_lib.enums.MessageRole;
import com.projects.distributed_lovable.common_lib.event.FileStoreRequestEvent;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.intelligence_service.client.WorkspaceClient;
import com.projects.distributed_lovable.intelligence_service.dto.chat.StreamResponse;
import com.projects.distributed_lovable.intelligence_service.entity.ChatEvent;
import com.projects.distributed_lovable.intelligence_service.entity.ChatMessage;
import com.projects.distributed_lovable.intelligence_service.entity.ChatSession;
import com.projects.distributed_lovable.intelligence_service.entity.ChatSessionId;
import com.projects.distributed_lovable.intelligence_service.llm.LlmResponseParser;
import com.projects.distributed_lovable.intelligence_service.llm.PromptUtils;
import com.projects.distributed_lovable.intelligence_service.llm.advisors.FileTreeContextAdvisor;
import com.projects.distributed_lovable.intelligence_service.llm.tools.CodeGenerationTool;
import com.projects.distributed_lovable.intelligence_service.repository.ChatEventRepository;
import com.projects.distributed_lovable.intelligence_service.repository.ChatMessageRepository;
import com.projects.distributed_lovable.intelligence_service.repository.ChatSessionRepository;
import com.projects.distributed_lovable.intelligence_service.service.AiGenerationService;
import com.projects.distributed_lovable.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.FileStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);
    private final FileTreeContextAdvisor fileTreeAdvisor;
    private final LlmResponseParser llmResponseParser;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatEventRepository chatEventRepository;
    private final UsageService usageService;
    private final WorkspaceClient workspaceClient;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponseFromLLM(String userMessage, Long projectId) {

        //usageService.checkDailyTokenUsage();
         Long userId = authUtil.getCurrentUserId();
         ChatSession chatSession =canCreateChatSessionIfNotExists(userId,projectId);
        Map<String,Object> advisorParams = Map.of(
                "userId",userId,
                    "projectId",projectId
        );
        CodeGenerationTool codeGenerationTool = new CodeGenerationTool(projectId,workspaceClient);
        StringBuilder fullResponseBuffer = new StringBuilder();

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTool)
                .advisors(advisorSpec -> {
                    advisorSpec.params(advisorParams);
                    advisorSpec.advisors(fileTreeAdvisor);
                })
                .stream()
                .chatResponse()
                .doOnNext(response->
                {
                    if(response.getResults() != null && !response.getResults().isEmpty()) {
                        if (response.getMetadata().getUsage() != null) {
                            usageRef.set(response.getMetadata().getUsage());
                        }
                        String content = response.getResult().getOutput().getText();
                        if (content != null && !content.isEmpty() && endTime.get() == 0) {
                            endTime.set(System.currentTimeMillis());
                        }
                        if (content != null) {
                            fullResponseBuffer.append(content);
                        }
                    }
                })
                .doOnComplete(()->{
                    Schedulers.boundedElastic().schedule(()->
                    {
                        //parseAndSaveFiles(fullResponseBuffer.toString(),projectId);
                        long duration = endTime.get()-startTime.get();
                        finalizeChats(userMessage,chatSession, fullResponseBuffer.toString(),duration,usageRef.get(),userId);
                    });
                    log.info("Final output :{}",fullResponseBuffer.toString());
                })
                .doOnError(response-> log.error("Error Occurred while streaming the response for project{}",projectId,response))
                // Final OpenAI chunk carries usage only (generations=[]), so getResult() is null -> drop it.
                .filter(response -> response != null && response.getResult() != null)
                .map(response -> {
                    if(response.getResults() != null && !response.getResults().isEmpty()) {
                        String text = response.getResult().getOutput().getText();
                        return new StreamResponse(text != null ? text : "");
                    }
                    return new StreamResponse("");
                })
                .filter(streamResponse -> !streamResponse.text().isEmpty());
    }


    public void finalizeChats(String userMessage, ChatSession chatSession,String fullText,Long duration,Usage usage,Long userId)
    {
          Long projectId = chatSession.getChatSessionId().getProjectId();
          if(usage != null)
          {
              int tokensUsed = usage.getTotalTokens();
              usageService.recordTokensUsage(userId,tokensUsed);
          }
           //store user message first
           chatMessageRepository.save(
                   ChatMessage.builder()
                           .chatSession(chatSession)
                           .role(MessageRole.USER)
                           .content(userMessage)
                           .tokensUsed(usage.getPromptTokens())
                           .build()
           );

           ChatMessage assistantChatMessage = chatMessageRepository.save(
                 ChatMessage.builder()
                         .chatSession(chatSession)
                         .role(MessageRole.ASSISTANT)
                         .content("Dummy message")
                         .tokensUsed(usage.getCompletionTokens())
                         .build()
           );


           List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText,assistantChatMessage);
           chatEventList.addFirst(ChatEvent
                .builder()
                           .chatMessage(assistantChatMessage)
                           .type(ChatEventType.THOUGHT)
                           .status(ChatEventStatus.CONFIRMED)
                           .content("Thought for "+duration+"s")
                           .sequenceOrder(0)
                   .build());
           chatEventList.stream()
                   .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
                   .forEach(e ->
                           {
                               String sagaId = UUID.randomUUID().toString();
                               e.setSagaId(sagaId);
                               FileStoreRequestEvent fileStoreRequestEvent = new FileStoreRequestEvent(projectId,sagaId,
                                       e.getFilePath(),e.getContent(),chatSession.getChatSessionId().getUserId());
                               log.info("Storage request event sent :{}",e.getFilePath());
                               kafkaTemplate.send("file-storage-request-event","project-"+projectId,fileStoreRequestEvent);
                           });
           chatEventRepository.saveAll(chatEventList);

    }

//    public void parseAndSaveFiles(String content, Long projectId)
//    {
//        Matcher matcher = FILE_TAG_PATTERN.matcher(content);
//        while(matcher.find())
//        {
//            String filePath = matcher.group(1);
//            String fileContent = matcher.group(2).trim();
//            projectFileService.saveFiles(projectId,filePath,fileContent);
//        }
//    }

    public ChatSession canCreateChatSessionIfNotExists(Long userId, Long projectId) {
        ChatSession chatSession = chatSessionRepository.findById(
                new ChatSessionId(projectId,userId)).orElse(null);
        if(chatSession == null)
        {
            ChatSessionId chatSessionId = new ChatSessionId(projectId,userId);
            chatSession = ChatSession.builder()
                    .chatSessionId(chatSessionId)
                    .build();
            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }
}
