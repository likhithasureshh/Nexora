package com.projects.distributed_lovable.intelligence_service.service;

public interface UsageService {
   void recordTokensUsage(Long userId,int actualTokens);
   void checkDailyTokenUsage();
}
