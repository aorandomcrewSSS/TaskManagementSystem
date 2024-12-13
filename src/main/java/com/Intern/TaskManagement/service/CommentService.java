package com.Intern.TaskManagement.service;

import com.Intern.TaskManagement.dto.request.CommentCreateRequest;
import com.Intern.TaskManagement.dto.response.CommentResponse;
import com.Intern.TaskManagement.model.Comment;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.model.enums.Role;
import com.Intern.TaskManagement.repository.CommentRepository;
import com.Intern.TaskManagement.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentResponse addComment(Long taskId, User author, CommentCreateRequest commentCreateRequest) throws AccessDeniedException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Проверка, что пользователь либо автор задачи, либо исполнитель
        if (!task.getAuthor().equals(author) && (task.getExecutor() == null || !task.getExecutor().equals(author))) {
            throw new AccessDeniedException("У пользователя нет прав на добавление комментария");
        }

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setText(commentCreateRequest.getText());
        commentRepository.save(comment);

        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                author.getId(),
                task.getId()
        );
    }

    public void deleteComment(Long commentId, User author) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        // Проверка, что пользователь либо автор задачи, либо исполнитель
        if (!comment.getAuthor().equals(author) && !isUserTaskAuthorOrExecutor(comment.getTask(), author)) {
            throw new AccessDeniedException("Только автор комментария или автор задачи/исполнитель может удалить комментарий");
        }

        commentRepository.delete(comment);
    }

    private boolean isUserTaskAuthorOrExecutor(Task task, User user) {
        return task.getAuthor().equals(user) || (task.getExecutor() != null && task.getExecutor().equals(user));
    }

    public List<CommentResponse> getCommentsByTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        return task.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthor().getId(),
                        taskId))
                .collect(Collectors.toList());
    }
}