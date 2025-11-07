package com.yourorg.aicode.config;

import com.yourorg.aicode.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenService.deleteExpiredTokens();
    }
}
