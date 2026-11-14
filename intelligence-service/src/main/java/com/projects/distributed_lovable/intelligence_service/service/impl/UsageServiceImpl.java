package com.projects.distributed_lovable.intelligence_service.service.impl;


import com.projects.distributed_lovable.common_lib.dto.PlanDto;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.intelligence_service.client.AccountClient;
import com.projects.distributed_lovable.intelligence_service.entity.UsageLog;
import com.projects.distributed_lovable.intelligence_service.repository.UsageLogRepository;
import com.projects.distributed_lovable.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final AuthUtil authUtil;
    private final AccountClient accountClient;

    @Override
    public void recordTokensUsage(Long userId, int actualTokens) {
        LocalDate todayDate = LocalDate.now();
        UsageLog usageLog = usageLogRepository.findByUserIdAndDate(userId,todayDate)
                .orElseGet(()->createNewDailyUsage(userId,todayDate));
        usageLog.setTokensUsed(usageLog.getTokensUsed() + actualTokens);
        usageLogRepository.save(usageLog);
    }

    @Override
    public void checkDailyTokenUsage() {
        Long userId = authUtil.getCurrentUserId();
        PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();
        LocalDate todayDate = LocalDate.now();
        if(plan.unlimitedAi())
        {
            return;
        }
        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId,todayDate)
                .orElseGet(()->createNewDailyUsage(userId,todayDate));
        int todayUsageToken = todayLog.getTokensUsed();
        int allowedTokenUsage = Math.toIntExact(plan.maxTokensPerDay());
        if(todayUsageToken >= allowedTokenUsage)
        {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Daily Limit Exceeded,Upgrade Now!");
        }
    }

    public UsageLog createNewDailyUsage(Long userId,LocalDate date)
    {
        return UsageLog
                .builder()
                .date(date)
                .userId(userId)
                .tokensUsed(0)
                .build();
    }
}
