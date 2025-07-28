package com.sfaai.sfaai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for task scheduling
 */
@Configuration
@Slf4j
public class SchedulingConfig {

    /**
     * Create a custom task scheduler with error handling
     * @return Configured task scheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // Set appropriate pool size
        scheduler.setThreadNamePrefix("vapi-scheduled-task-");
        scheduler.setErrorHandler(throwable -> {
            // Handle scheduling errors without letting them crash the scheduler
            log.error("Error in scheduled task: {}", throwable.getMessage(), throwable);
        });
        scheduler.setRejectedExecutionHandler((runnable, executor) -> {
            // Log when tasks are rejected due to queue capacity
            log.warn("Task rejected: Queue capacity reached or shutdown in progress");
        });
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
