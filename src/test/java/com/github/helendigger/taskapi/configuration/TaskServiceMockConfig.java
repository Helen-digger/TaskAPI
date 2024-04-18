package com.github.helendigger.taskapi.configuration;

import com.github.helendigger.taskapi.service.TaskService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
public class TaskServiceMockConfig {
    @Bean
    @Profile("test")
    @Primary
    public TaskService getTaskServiceMock() {
        return Mockito.mock(TaskService.class);
    }
}
