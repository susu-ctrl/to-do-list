package com.org.todo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Data
public class TaskUpdateRequest {

    @Size(max = 200, message = "任务标题不超过200字")
    private String title;

    private String description;

    @Range(min = 0, max = 2, message = "优先级范围为0-2")
    private Integer priority;

    private Long categoryId;

    private LocalDateTime dueDate;

    @Range(min = 0, message = "提醒偏移量不能为负数")
    private Integer remindOffset;
}