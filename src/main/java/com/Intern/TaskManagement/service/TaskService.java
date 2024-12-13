package com.Intern.TaskManagement.service;

import com.Intern.TaskManagement.dto.mapper.TaskMapper;
import com.Intern.TaskManagement.dto.request.TaskCreateRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateByExecutorRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateRequest;
import com.Intern.TaskManagement.dto.response.TaskResponse;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.model.enums.Role;
import com.Intern.TaskManagement.repository.TaskRepository;
import com.Intern.TaskManagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;


    public TaskResponse createTask(TaskCreateRequest taskCreateRequest, User author) throws AccessDeniedException {
        User executor = null;
        if (taskCreateRequest.getExecutorId() != null) {
            executor = userRepository.findById(taskCreateRequest.getExecutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Executor not found"));
        }

        if (author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Только администратор может создать задачу");
        }

        Task task = taskMapper.taskCreateRequestToTask(taskCreateRequest, author, executor);
        taskRepository.save(task);

        return taskMapper.taskToTaskResponse(task);
    }


    public TaskResponse updateTask(Long taskId, TaskUpdateRequest taskUpdateRequest, User user) throws AccessDeniedException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Логика проверки доступа должна идти до поиска исполнителя
        if (!task.getAuthor().equals(user) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Только администратор может изменить задачу");
        }

        // Если все проверки прошли, то выполняем поиск исполнителя
        User executor = null;
        if (taskUpdateRequest.getExecutorId() != null) {
            executor = userRepository.findById(taskUpdateRequest.getExecutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Исполнитель не найден"));
        }

        // Маппим обновленные данные из DTO
        task = taskMapper.taskUpdateRequestToTask(taskUpdateRequest, task, executor);

        // Сохраняем обновленную задачу
        Task updatedTask = taskRepository.save(task);
        return taskMapper.taskToTaskResponse(updatedTask);  // Возвращаем TaskResponse
    }

    public TaskResponse changeMyTasks(Long taskId, TaskUpdateByExecutorRequest taskUpdateByExecutorRequest, User user) throws AccessDeniedException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Проверяем, является ли пользователь исполнителем задачи
        if (task.getExecutor() == null || !task.getExecutor().equals(user)) {
            throw new AccessDeniedException("Вы не являетесь исполнителем этой задачи");
        }

        // Маппим обновления из DTO
        task = taskMapper.taskUpdateByExecutorRequestToTask(taskUpdateByExecutorRequest, task);

        // Сохраняем обновленную задачу
        Task updatedTask = taskRepository.save(task);
        return taskMapper.taskToTaskResponse(updatedTask);  // Возвращаем TaskResponse
    }

    // Получение задачи по ID
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        return taskMapper.taskToTaskResponse(task); // Возвращаем TaskResponse
    }

    public Page<TaskResponse> getTasksByExecutor(Long executorId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByExecutorId(executorId, pageable);
        return tasks.map(taskMapper::taskToTaskResponse);
    }

    public Page<TaskResponse> getTasksByAuthor(Long authorId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAuthorId(authorId, pageable);
        return tasks.map(taskMapper::taskToTaskResponse);
    }

    // Удаление задачи
    public void deleteTask(Long taskId, User user) throws AccessDeniedException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Проверяем, что пользователь может удалять задачу
        if (!task.getAuthor().equals(user) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Только администратор может удалить задачу");
        }

        taskRepository.delete(task); // Удаляем задачу
    }
}
