package com.sfaai.sfaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for task scheduling
 */
@Configuration
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
            System.err.println("Error in scheduled task: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        scheduler.setRejectedExecutionHandler((runnable, executor) -> {
            // Log when tasks are rejected due to queue capacity
            System.err.println("Task rejected: Queue capacity reached or shutdown in progress");
        });
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
