package com.github.helendigger.taskapi.service;

import com.github.helendigger.taskapi.configuration.TaskRepositoryMockConfig;
import com.github.helendigger.taskapi.dto.TaskDTO;
import com.github.helendigger.taskapi.model.Task;
import com.github.helendigger.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = TaskRepositoryMockConfig.class)
@ActiveProfiles("test")
public class TaskServiceTest {
    @Autowired
    TaskService taskService;

    @Autowired
    TaskRepository taskRepository;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(taskRepository);
    }

    @Test
    public void testCreateTask() {
        var taskDto = TaskDTO.builder()
                .title("First task")
                .description("First task description")
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true).build();

        var expectedTask = new Task();
        expectedTask.setTitle(taskDto.getTitle());
        expectedTask.setDescription(taskDto.getDescription());
        expectedTask.setDueDate(taskDto.getDueDate());
        expectedTask.setCompleted(taskDto.getCompleted());

        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);

        Mockito.when(taskRepository.saveAndFlush(taskArgumentCaptor.capture())).thenAnswer(answer -> {
            Task convertedTask = taskArgumentCaptor.getValue();
            Assertions.assertEquals(expectedTask, convertedTask);
            convertedTask.setId(1L);
            return convertedTask;
        });

        Long taskId = taskService.createTask(taskDto);
        Assertions.assertEquals(1L, taskId);
    }

    @Test
    public void testGetAllTasks() {
        var firstTask = new Task();
        firstTask.setId(1L);
        firstTask.setTitle("First task");
        firstTask.setDescription("First task description");
        firstTask.setDueDate(LocalDateTime.now());
        firstTask.setCompleted(true);

        var secondTask = new Task();
        secondTask.setId(2L);
        secondTask.setTitle("Second task");
        secondTask.setDescription("Second task description");
        secondTask.setDueDate(LocalDateTime.now());
        secondTask.setCompleted(false);

        var firstTaskDto = TaskDTO.builder()
                .id(firstTask.getId())
                .title(firstTask.getTitle())
                .description(firstTask.getDescription())
                .dueDate(firstTask.getDueDate())
                .completed(firstTask.getCompleted()).build();
        var secondTaskDto = TaskDTO.builder()
                .id(secondTask.getId())
                .title(secondTask.getTitle())
                .description(secondTask.getDescription())
                .dueDate(secondTask.getDueDate())
                .completed(secondTask.getCompleted()).build();

        var expectedTasks = List.of(firstTask, secondTask);
        var expectedTasksDto = List.of(firstTaskDto, secondTaskDto);

        Mockito.when(taskRepository.getAll()).thenReturn(expectedTasks);

        var dtoTasks = taskService.getAllTasks();
        Assertions.assertEquals(expectedTasksDto, dtoTasks.tasks());
    }

    @Test
    public void removeByIdTask() {
        taskService.removeById(1L);
        Mockito.verify(taskRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    public void updateByIdTask() {
        var taskDto = TaskDTO.builder()
                .title("First task")
                .description("First task description")
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true).build();

        var firstTask = new Task();
        firstTask.setId(1L);
        firstTask.setTitle("First task");
        firstTask.setDescription("First task description");
        firstTask.setDueDate(LocalDateTime.now());
        firstTask.setCompleted(true);

        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.of(firstTask));
        Mockito.when(taskRepository.saveAndFlush(firstTask)).thenReturn(firstTask);

        taskService.updateTask(1L, taskDto);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(taskRepository, Mockito.times(1)).saveAndFlush(firstTask);
    }

    @Test
    public void updateByIdEmpty() {
        Mockito.when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        var taskDto = TaskDTO.builder()
                .title("First task")
                .description("First task description")
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true).build();
        taskService.updateTask(1L, taskDto);

        Mockito.verify(taskRepository, Mockito.atMostOnce()).findById(1L);
        Mockito.verify(taskRepository, Mockito.never()).saveAndFlush(Mockito.any());
    }
}
