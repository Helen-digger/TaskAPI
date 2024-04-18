package com.github.helendigger.taskapi.service;

import com.github.helendigger.taskapi.dto.TaskDTO;
import com.github.helendigger.taskapi.dto.Tasks;
import com.github.helendigger.taskapi.model.Task;
import com.github.helendigger.taskapi.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Tasks getAllTasks() {
        return new Tasks(taskRepository.getAll().stream()
                .map(TaskService::convertFromTask).collect(Collectors.toList()));
    }

    public Long createTask(TaskDTO task) {
        var converted = convertFromTaskDTO(task);
        var saved = taskRepository.saveAndFlush(converted);
        return saved.getId();
    }

    @Transactional
    public void updateTask(Long taskId, TaskDTO task) {
        taskRepository.findById(taskId)
                .ifPresent(found -> {
                    updateTaskWithDTO(found, task);
                    taskRepository.saveAndFlush(found);
                });
    }

    public Optional<TaskDTO> getById(Long taskId) {
        return taskRepository.findById(taskId).map(TaskService::convertFromTask);
    }

    public void removeById(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    private Task convertFromTaskDTO(TaskDTO taskDTO) {
        Task task = new Task();
        task.setCompleted(taskDTO.getCompleted());
        task.setDescription(taskDTO.getDescription());
        task.setTitle(taskDTO.getTitle());
        task.setDueDate(taskDTO.getDueDate());
        return task;
    }

    private static TaskDTO convertFromTask(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setDueDate(task.getDueDate());
        taskDTO.setCompleted(task.getCompleted());
        return taskDTO;
    }

    private static void updateTaskWithDTO(Task to, TaskDTO from) {
        Optional.ofNullable(from.getTitle()).ifPresent(to::setTitle);
        Optional.ofNullable(from.getDescription()).ifPresent(to::setDescription);
        Optional.ofNullable(from.getDueDate()).ifPresent(to::setDueDate);
        Optional.ofNullable(from.getCompleted()).ifPresent(to::setCompleted);
    }
}
