package com.Intern.TaskManagement.dto.mapper;

import com.Intern.TaskManagement.dto.request.CommentCreateRequest;
import com.Intern.TaskManagement.dto.response.CommentResponse;
import com.Intern.TaskManagement.model.Comment;
import com.Intern.TaskManagement.model.Task;
import com.Intern.TaskManagement.model.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment commentCreateRequestToComment(CommentCreateRequest commentCreateRequest, Task task, User author) {
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setText(commentCreateRequest.getText());
        return comment;
    }

    public CommentResponse commentToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setAuthorId(comment.getAuthor().getId());
        response.setTaskId(comment.getTask().getId());
        return response;
    }
}