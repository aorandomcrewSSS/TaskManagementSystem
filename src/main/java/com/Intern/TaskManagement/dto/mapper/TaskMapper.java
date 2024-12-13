package com.Intern.TaskManagement.dto.mapper;

import com.Intern.TaskManagement.dto.request.TaskCreateRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateByExecutorRequest;
import com.Intern.TaskManagement.dto.request.TaskUpdateRequest;
import com.Intern.TaskManagement.dto.response.CommentResponse;
import com.Intern.TaskManagement.dto.response.TaskResponse;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import com.Intern.TaskManagement.model.enums.Priority;
import com.Intern.TaskManagement.model.enums.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task taskCreateRequestToTask(TaskCreateRequest taskCreateRequest, User author, User executor) {
        Task task = new Task();
        task.setTitle(taskCreateRequest.getTitle());
        task.setDescription(taskCreateRequest.getDescription());
        task.setStatus(taskCreateRequest.getStatus() != null
                ? Status.valueOf(taskCreateRequest.getStatus())
                : Status.PENDING);
        task.setPriority(taskCreateRequest.getPriority() != null
                ? Priority.valueOf(taskCreateRequest.getPriority())
                : Priority.HIGH);  // Преобразуем строку в Enum
        task.setAuthor(author);  // Автор задачи задается из текущего пользователя
        task.setExecutor(executor);
        task.setComments(new ArrayList<>());  // Игнорируем комментарии
        return task;
    }

    public Task taskUpdateRequestToTask(TaskUpdateRequest taskUpdateRequest, Task existingTask, User executor) {
        existingTask.setStatus(Status.valueOf(taskUpdateRequest.getStatus()));  // Преобразуем строку в Enum
        existingTask.setPriority(Priority.valueOf(taskUpdateRequest.getPriority()));  // Преобразуем строку в Enum
        existingTask.setExecutor(executor);
        return existingTask;
    }

    public Task taskUpdateByExecutorRequestToTask(TaskUpdateByExecutorRequest taskUpdateByExecutorRequest, Task existingTask){
        existingTask.setStatus(Status.valueOf(taskUpdateByExecutorRequest.getStatus()));
        existingTask.setPriority(Priority.valueOf(taskUpdateByExecutorRequest.getPriority()));
        return existingTask;
    }
    public TaskResponse taskToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setPriority(task.getPriority().name());
        response.setAuthorId(task.getAuthor().getId());
        response.setExecutorId(task.getExecutor() != null ? task.getExecutor().getId() : null);

        List<CommentResponse> commentResponses = task.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthor().getId(),
                        comment.getTask().getId()))
                .collect(Collectors.toList());
        response.setComments(commentResponses);
        return response;
    }
}
