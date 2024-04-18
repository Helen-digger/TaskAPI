package com.github.helendigger.taskapi.dto;

/**
 * Entity used to return to the user after task creation
 * @param id id of the newly created task
 */
public record TaskId(Long id) {
}
