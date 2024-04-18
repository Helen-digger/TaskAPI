package com.github.helendigger.taskapi.configuration;

import com.github.helendigger.taskapi.repository.TaskRepository;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class TaskRepositoryMockConfig {
    @Bean
    @Profile("test")
    public TaskRepository getTaskRepositoryMock() {
        return Mockito.mock(TaskRepository.class);
    }
}
