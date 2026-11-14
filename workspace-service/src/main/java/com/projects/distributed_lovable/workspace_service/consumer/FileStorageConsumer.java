package com.projects.distributed_lovable.workspace_service.consumer;

import com.projects.distributed_lovable.common_lib.event.FileStoreRequestEvent;
import com.projects.distributed_lovable.common_lib.event.FileStoreResponseEvent;
import com.projects.distributed_lovable.workspace_service.entity.ProcessedEvents;
import com.projects.distributed_lovable.workspace_service.repository.ProcessedEventsRepository;
import com.projects.distributed_lovable.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {
    private final ProjectFileService projectFileService;
    private final ProcessedEventsRepository processedEventsRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "file-storage-request-event",groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent fileStoreRequestEvent)
    {
        if(processedEventsRepository.existsById(fileStoreRequestEvent.sagaId()))
        {
            log.info("Duplicate saga detected:{},Resending Previous ACK.",fileStoreRequestEvent.sagaId());
            sendResponse(fileStoreRequestEvent,true,null);
            return;
        }
        try
        {
            log.info("Saving file:{}",fileStoreRequestEvent.filePath());
            projectFileService.saveFiles(fileStoreRequestEvent.projectId(),
                    fileStoreRequestEvent.filePath(),fileStoreRequestEvent.content());
            processedEventsRepository.save(new ProcessedEvents(fileStoreRequestEvent.sagaId(), LocalDateTime.now()));
            sendResponse(fileStoreRequestEvent,true,null);
        }
        catch(Exception e)
        {
            log.error("Error saving the file:{}",e.getMessage());
            sendResponse(fileStoreRequestEvent,false,e.getMessage());
        }

    }

    public void sendResponse(FileStoreRequestEvent fileStoreRequestEvent,Boolean success,String error)
    {
        FileStoreResponseEvent fileStoreResponseEvent = FileStoreResponseEvent.builder()
                .sagaId(fileStoreRequestEvent.sagaId())
                .success(success)
                .projectId(fileStoreRequestEvent.projectId())
                .errorMessage(error)
                .build();
        kafkaTemplate.send("file-store-responses",fileStoreResponseEvent);
    }


}
