package com.github.helendigger.taskapi.dto;

import com.github.helendigger.taskapi.dto.validation.TaskCreation;
import com.github.helendigger.taskapi.dto.validation.TaskEditing;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Task DTO used to describe creating and editing scenarios of the task
 * Id field from the user is ignored and used only as view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    @NotNull(message = "Task title must be present", groups = TaskCreation.class)
    @Size(message = "Task title must be between 3 and 255 characters",min = 3, max = 255,
            groups = {TaskCreation.class, TaskEditing.class})
    private String title;

    @NotNull(message = "Task description must be present", groups = TaskCreation.class)
    @Size(message = "Task description must be at most 1024 characters", max = 1024,
            groups = {TaskCreation.class, TaskEditing.class})
    private String description;

    @FutureOrPresent(message = "Due date must be at least in present time or in future", groups = TaskCreation.class)
    @NotNull(message = "The due date must be present", groups = TaskCreation.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDate;

    @Builder.Default
    private Boolean completed = false;
}
