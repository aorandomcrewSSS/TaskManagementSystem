package com.Intern.TaskManegment.sevice;

import com.Intern.TaskManagement.dto.mapper.TaskMapper;
import com.Intern.TaskManagement.dto.request.TaskCreateRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateRequest;
import com.Intern.TaskManagement.dto.response.TaskResponse;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.model.enums.Priority;
import com.Intern.TaskManagement.model.enums.Role;
import com.Intern.TaskManagement.model.enums.Status;
import com.Intern.TaskManagement.repository.TaskRepository;
import com.Intern.TaskManagement.repository.UserRepository;
import com.Intern.TaskManagement.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;


    @Test
    void createTask_ExecutorNotFound() {
        User author = new User(1L, "Author", "author@example.com", "password", Role.USER, List.of());
        TaskCreateRequest request = new TaskCreateRequest("Task Title", "Task Description", "PENDING", "HIGH", 2L);

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.createTask(request, author));
        verify(userRepository).findById(2L);
        verifyNoInteractions(taskMapper, taskRepository);
    }

    @Test
    void updateTask_AccessDenied() {

        User user = new User(1L, "User", "user@example.com", "password", Role.USER, List.of());
        Task task = new Task(1L, "Task Title", "Task Description", Status.PENDING, Priority.HIGH, new User(2L, "Another Author", "author@example.com", "password", Role.USER, List.of()), null, List.of());
        TaskUpdateRequest request = new TaskUpdateRequest("IN_PROGRESS", "MEDIUM", null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(1L, request, user));
        verify(taskRepository).findById(1L);
        verifyNoInteractions(userRepository, taskMapper);
    }

    @Test
    void deleteTask_Success() throws AccessDeniedException {
        User admin = new User(1L, "Admin", "admin@example.com", "password", Role.ADMIN, List.of());
        Task task = new Task(1L, "Task Title", "Task Description", Status.PENDING, Priority.HIGH, admin, null, List.of());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, admin);

        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }
}