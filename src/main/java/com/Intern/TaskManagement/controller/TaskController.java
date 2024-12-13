package com.Intern.TaskManagement.controller;

import com.Intern.TaskManagement.dto.request.TaskCreateRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateByExecutorRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateRequest;
import com.Intern.TaskManagement.dto.response.TaskResponse;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.repository.UserRepository;
import com.Intern.TaskManagement.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Api(tags = "Управление задачами", description = "Операции по управлению задачами")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
    private final PagedResourcesAssembler<TaskResponse> pagedResourcesAssembler;;

    @PostMapping
    @ApiOperation(value = "Создать новую задачу", notes = "Создает задачу и назначает ее исполнителю, если указан.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Задача успешно создана", response = TaskResponse.class),
            @ApiResponse(code = 400, message = "Неверные данные ввода")
    })
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskCreateRequest taskCreateRequest) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        TaskResponse taskResponse = taskService.createTask(taskCreateRequest, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    @PutMapping("/update-task/{taskId}")
    @ApiOperation(value = "Обновить существующую задачу", notes = "Обновляет статус, приоритет или исполнителя существующей задачи.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Задача успешно обновлена", response = TaskResponse.class),
            @ApiResponse(code = 403, message = "Отказано в праве на обновление этой задачи"),
            @ApiResponse(code = 404, message = "Задача не найдена")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest taskUpdateRequest) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        TaskResponse taskResponse = taskService.updateTask(taskId, taskUpdateRequest, user);
        return ResponseEntity.ok(taskResponse);
    }

    @PutMapping("/update-my-task/{taskId}")
    @ApiOperation(value = "Изменить статус и приоритет задачи", notes = "Изменяет статус и приоритет задачи, если текущий пользователь является исполнителем задачи.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Задача успешно обновлена", response = TaskResponse.class),
            @ApiResponse(code = 403, message = "Отказано в праве на обновление этой задачи, если не является исполнителем"),
            @ApiResponse(code = 404, message = "Задача не найдена")
    })
    public ResponseEntity<TaskResponse> changeMyTasks(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateByExecutorRequest taskUpdateByExecutorRequest) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        TaskResponse taskResponse = taskService.changeMyTasks(taskId, taskUpdateByExecutorRequest, user);
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping("/get-task/{taskId}")
    @ApiOperation(value = "Получить задачу по ID", notes = "Получает детали конкретной задачи по ее ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Задача успешно получена", response = TaskResponse.class),
            @ApiResponse(code = 404, message = "Задача не найдена")
    })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        TaskResponse taskResponse = taskService.getTaskById(taskId);
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping("/executor/{executorId}")
    @ApiOperation(value = "Получить задачи по исполнителю", notes = "Получает все задачи, назначенные конкретному исполнителю.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Задачи успешно получены", response = Page.class)
    })
    public ResponseEntity<PagedModel<EntityModel<TaskResponse>>> getTasksByExecutor(
            @PathVariable Long executorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Проверка, существует ли исполнитель с таким ID
        User executor = userRepository.findById(executorId)
                .orElseThrow(() -> new EntityNotFoundException("Исполнитель не найден"));

        Page<TaskResponse> tasks = taskService.getTasksByExecutor(executorId, pageable);

        // Используем PagedResourcesAssembler для преобразования Page в PagedModel
        PagedModel<EntityModel<TaskResponse>> pagedModel = pagedResourcesAssembler.toModel(tasks);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/my-tasks")
    @ApiOperation(value = "Получить задачи по автору", notes = "Получает все задачи, созданные текущим авторизованным пользователем.")
    @ApiResponse(code = 200, message = "Задачи успешно получены", response = Page.class)
    public ResponseEntity<PagedModel<EntityModel<TaskResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Автор не найден"));

        Page<TaskResponse> tasks = taskService.getTasksByAuthor(author.getId(), pageable);

        // Используем PagedResourcesAssembler для преобразования Page в PagedModel
        PagedModel<EntityModel<TaskResponse>> pagedModel = pagedResourcesAssembler.toModel(tasks);
        return ResponseEntity.ok(pagedModel);
    }

    @DeleteMapping("/delete-task/{taskId}")
    @ApiOperation(value = "Удалить задачу", notes = "Удаляет конкретную задачу, если текущий пользователь является автором или имеет права администратора.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Задача успешно удалена"),
            @ApiResponse(code = 403, message = "Отказано в праве на удаление этой задачи"),
            @ApiResponse(code = 404, message = "Задача не найдена")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        taskService.deleteTask(taskId, user);
        return ResponseEntity.noContent().build();
    }
}
