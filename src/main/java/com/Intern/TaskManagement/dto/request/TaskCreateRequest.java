package com.Intern.TaskManagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {
    private String title;
    private String description;
    @Schema(
            description = "Status of the task",
            defaultValue = "PENDING",
            example = "PENDING"
    )
    private String status;
    @Schema(
            description = "Priority of the task",
            defaultValue = "HIGH",
            example = "HIGH"
    )
    private String priority; // можно использовать enum, если нужно
    private Long executorId;
}
