package com.Intern.TaskManagement.controller;

import com.Intern.TaskManagement.dto.request.CommentCreateRequest;
import com.Intern.TaskManagement.dto.response.CommentResponse;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.repository.UserRepository;
import com.Intern.TaskManagement.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Api(tags = "Управление комментариями", description = "Операции по управлению комментариями к задачам")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    @PostMapping("/task/{taskId}")
    @ApiOperation(value = "Добавить комментарий к задаче", notes = "Добавляет комментарий к указанной задаче")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Комментарий успешно добавлен", response = CommentResponse.class),
            @ApiResponse(code = 400, message = "Неверный ввод", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Неавторизованный доступ", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Задача не найдена", response = ErrorResponse.class)
    })
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentCreateRequest commentCreateRequest) throws AccessDeniedException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        CommentResponse commentResponse = commentService.addComment(taskId, author, commentCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);
    }

    @GetMapping("/task/{taskId}")
    @ApiOperation(value = "Получить комментарии к задаче", notes = "Получает все комментарии, связанные с указанной задачей")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Комментарии успешно получены", response = CommentResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Задача не найдена", response = ErrorResponse.class)
    })
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@PathVariable Long taskId) {
        List<CommentResponse> commentResponses = commentService.getCommentsByTask(taskId);

        return ResponseEntity.ok(commentResponses);
    }

    @DeleteMapping("/{commentId}")
    @ApiOperation(value = "Удалить комментарий", notes = "Удаляет указанный комментарий")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Комментарий успешно удален"),
            @ApiResponse(code = 401, message = "Неавторизованный доступ", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Комментарий не найден", response = ErrorResponse.class)
    })
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        commentService.deleteComment(commentId, author);
        return ResponseEntity.noContent().build();
    }
}