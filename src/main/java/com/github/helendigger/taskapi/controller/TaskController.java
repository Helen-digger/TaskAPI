package com.github.helendigger.taskapi.controller;

import com.github.helendigger.taskapi.constant.CacheConst;
import com.github.helendigger.taskapi.dto.TaskDTO;
import com.github.helendigger.taskapi.dto.TaskId;
import com.github.helendigger.taskapi.dto.Tasks;
import com.github.helendigger.taskapi.dto.validation.TaskCreation;
import com.github.helendigger.taskapi.dto.validation.TaskEditing;
import com.github.helendigger.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API for task management
 */
@RestController
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Get all tasks that
     * @return list of all tasks
     */
    @Operation(summary = "Get all tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all tasks", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Tasks.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = CacheConst.ALL_TASKS_CACHE_NAME)
    public ResponseEntity<Tasks> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    /**
     * Get one task by ID. If not found return 404.
     * @param id id of the task to get
     * @return task or 404 if not found
     */
    @Operation(summary = "Get task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task got by id", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskDTO.class))
            }),
            @ApiResponse(responseCode = "404", description = "Task not found by id", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/tasks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = CacheConst.TASK_CACHE_NAME, key = "args[0]")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable
                                                   @Min(1)
                                                   @Parameter(description = "id of the task") Long id) {
        return taskService.getById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create task by provided description
     * @param task task with creating fields
     * @return id of the created task
     */
    @Operation(summary = "Create task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Id of the created task", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskId.class))
            }),
            @ApiResponse(responseCode = "404", description = "Task not found by id", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/tasks",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = CacheConst.ALL_TASKS_CACHE_NAME)
    public ResponseEntity<TaskId> createTask(@Parameter(description = "task body to create, id is ignored")
                                                 @RequestBody @Validated(TaskCreation.class) TaskDTO task) {
        var taskId = taskService.createTask(task);
        return ResponseEntity.created(URI.create("/tasks/" + taskId)).body(new TaskId(taskId));
    }

    /**
     * Update task by id
     * @param id id of the task to update
     * @param task task body with field to update
     * @return 204
     */
    @Operation(summary = "Update task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operation result, no content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(value = "/tasks/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConst.ALL_TASKS_CACHE_NAME),
            @CacheEvict(cacheNames = CacheConst.TASK_CACHE_NAME, key = "args[0]")})
    public ResponseEntity<Void> updateTask(@Parameter(description = "id of the task to update")
                                                 @PathVariable @Min(1) Long id,
                                             @Parameter(description = "task body containing field to update")
                                             @RequestBody  @Validated(TaskEditing.class) TaskDTO task) {
        taskService.updateTask(id, task);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete task by ID
     * @param id id of the task
     * @return 204
     */
    @Operation(summary = "Delete task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Operation result, no content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping(value = "/tasks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConst.ALL_TASKS_CACHE_NAME),
            @CacheEvict(cacheNames = CacheConst.TASK_CACHE_NAME, key = "args[0]")})
    public ResponseEntity<Void> deleteTaskById(@Parameter(description = "id of the task to delete")
                                                   @PathVariable @Min(1) Long id) {
        taskService.removeById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handle validation errors, return bad request and a map with invalid values
     * @param exception exception to handle
     * @return response entity with bad request status and a map of invalid values and reason why they are invalid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequestArguments(MethodArgumentNotValidException exception) {
        var validationErrors = exception.getBindingResult().getAllErrors().stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .collect(Collectors.toMap(FieldError::getField, e -> Optional.ofNullable(e.getDefaultMessage())
                        .orElseGet(() -> "Invalid")));
        return ResponseEntity.badRequest().body(validationErrors);
    }

    /**
     * Handle argument error, return bad request and an object {"error" : "description"} back to user
     * @param exception exception to handle
     * @return response entity with bad request status and object with error
     */
    @ExceptionHandler(value = {MethodArgumentConversionNotSupportedException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleInvalidConversion(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    /**
     * Every other error that is not validation or parsing request should be treated as Internal
     * @param throwable error that occurred inside the service
     * @return response entity with internal server error and object with error
     */
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleGenericException(Throwable throwable) {
        return ResponseEntity.internalServerError().body(Map.of("error", throwable.getMessage()));
    }
}
