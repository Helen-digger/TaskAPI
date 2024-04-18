package com.github.helendigger.taskapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.helendigger.taskapi.configuration.TaskServiceMockConfig;
import com.github.helendigger.taskapi.dto.TaskDTO;
import com.github.helendigger.taskapi.dto.TaskId;
import com.github.helendigger.taskapi.dto.Tasks;
import com.github.helendigger.taskapi.service.TaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebMvcTest(TaskController.class)
@ContextConfiguration(classes = TaskServiceMockConfig.class)
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    TaskService serviceMock;

    @Autowired
    ObjectMapper mapper;

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(serviceMock);
    }

    @Test
    public void getAllTasksTest() {
        var expectedItems = new Tasks(List.of(TaskDTO
                .builder()
                .id(1L)
                .dueDate(LocalDateTime.now())
                .completed(true)
                .title("First task")
                .description("First task description").build(), TaskDTO
                .builder()
                .id(2L)
                .dueDate(LocalDateTime.now())
                .title("Second task")
                .description("Second task description").build()));

        Mockito.when(serviceMock.getAllTasks()).thenReturn(expectedItems);

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedItems))));
    }

    @Test
    void getEmptyTasksTest() {
        var expectedItems = new Tasks(Collections.emptyList());

        Mockito.when(serviceMock.getAllTasks()).thenReturn(expectedItems);

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedItems))));
    }

    @Test
    void getByIdTest() {
        var expectedItem = TaskDTO.builder()
                .id(1L)
                .title("First task")
                .description("First task description")
                .dueDate(LocalDateTime.now())
                .completed(true).build();

        Mockito.when(serviceMock.getById(1L)).thenReturn(Optional.of(expectedItem));

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedItem))));
    }

    @Test
    void getByIdTestNotFound() {
        Mockito.when(serviceMock.getById(1L)).thenReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound()));
    }

    @Test
    void getByIdTestBadRequest() {
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks/notValidId")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void getByIdTestInternal() {
        var targetException = new RuntimeException("Some internal error");
        Mockito.when(serviceMock.getById(1L)).thenThrow(targetException);
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .get("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(Map.of("error", targetException.getMessage())))));
    }

    @Test
    void createTaskTest() {
        var task = TaskDTO
                .builder()
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true)
                .title("First task")
                .description("First task description").build();
        var taskId = new TaskId(1L);
        Mockito.when(serviceMock.createTask(task)).thenReturn(taskId.id());

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                .post("/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(task)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(taskId))));
    }

    @Test
    void createTaskValidationDateTest() {
        var task = TaskDTO
                .builder()
                .dueDate(LocalDateTime.now().minusMinutes(1))
                .completed(true)
                .title("First task")
                .description("First task description").build();
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(task)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void createTaskValidationTitleTest() {
        var task = TaskDTO
                .builder()
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true)
                .title("Fi")
                .description("First task description").build();
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(task)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void updateTaskTest() {
        var task = TaskDTO
                .builder()
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true)
                .title("First task")
                .description("First task description").build();

        Mockito.doAnswer(answer -> {
            Long taskId = answer.getArgument(0);
            Assertions.assertEquals(1L, taskId);
            TaskDTO taskFromController = answer.getArgument(1);
            Assertions.assertEquals(task, taskFromController);
            return answer;
        }).when(serviceMock).updateTask(1L, task);

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                .put("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(task)))
                .andExpect(MockMvcResultMatchers.status().isNoContent()));
    }

    @Test
    void updateTaskValidation() {
        var task = TaskDTO
                .builder()
                .dueDate(LocalDateTime.now().plusMinutes(1))
                .completed(true)
                .title("Fi")
                .description("First task description").build();

        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(task)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()));
    }

    @Test
    void deleteTaskTest() {
        Assertions.assertDoesNotThrow(() -> mockMvc.perform(MockMvcRequestBuilders
                        .delete("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent()));
        Mockito.verify(serviceMock).removeById(1L);
    }
}
