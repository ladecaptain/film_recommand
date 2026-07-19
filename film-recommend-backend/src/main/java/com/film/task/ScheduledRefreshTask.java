package com.film.task;

import com.film.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledRefreshTask {

    private final CacheService cacheService;

    public ScheduledRefreshTask(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshPopular() {
        cacheService.refreshPopularCache();
    }
}
