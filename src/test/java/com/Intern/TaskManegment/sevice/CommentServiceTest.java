package com.Intern.TaskManegment.sevice;

import com.Intern.TaskManagement.dto.request.CommentCreateRequest;
import com.Intern.TaskManagement.dto.response.CommentResponse;
import com.Intern.TaskManagement.model.Comment;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.model.enums.Priority;
import com.Intern.TaskManagement.model.enums.Role;
import com.Intern.TaskManagement.model.enums.Status;
import com.Intern.TaskManagement.repository.CommentRepository;
import com.Intern.TaskManagement.repository.TaskRepository;
import com.Intern.TaskManagement.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addComment_Success() throws AccessDeniedException {
        // Arrange
        User author = new User(1L, "Author", "author@example.com", "password", Role.USER, List.of());
        Task task = new Task(1L, "Task Title", "Task Description", Status.PENDING, Priority.HIGH, author, null, List.of());
        CommentCreateRequest request = new CommentCreateRequest("New comment");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("New comment");
        comment.setAuthor(author);
        comment.setTask(task);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(Mockito.any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(1L);  // Mock setting the ID of the saved comment
            return savedComment;
        });

        // Act
        CommentResponse result = commentService.addComment(1L, author, request);

        // Assert
        assertAll(
                () -> assertEquals(1L, result.getId(), "Comment ID should match"),
                () -> assertEquals("New comment", result.getText(), "Comment text should match"),
                () -> assertEquals(1L, result.getAuthorId(), "Author ID should match"),
                () -> assertEquals(1L, result.getTaskId(), "Task ID should match")
        );

        verify(taskRepository).findById(1L);  // Verify task lookup
        verify(commentRepository).save(Mockito.any(Comment.class));  // Verify saving the comment
    }


    @Test
    void deleteComment_AccessDenied() {
        // Arrange
        User author = new User(1L, "Author", "author@example.com", "password", Role.USER, List.of());
        User otherUser = new User(2L, "Other User", "other@example.com", "password", Role.USER, List.of());
        Task task = new Task(1L, "Task Title", "Task Description", Status.PENDING, Priority.HIGH, author, null, List.of());  // Mock a task
        Comment comment = new Comment(1L, task, author, "Comment Text");  // Associate the comment with the task

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(1L, otherUser));

        // Verify interactions
        verify(commentRepository).findById(1L);
    }
}