package com.github.helendigger.taskapi.dto;

import java.util.List;

/**
 * List of all tasks
 * @param tasks list of all tasks
 */
public record Tasks(List<TaskDTO> tasks) {
}
