package com.org.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不超过200字")
    private String title;

    private String description;

    @Range(min = 0, max = 2, message = "优先级范围为0-2")
    private Integer priority = 1;  // 默认中优先级

    private Long categoryId;  // 可选，不选则为null

    private LocalDateTime dueDate;  // 可选

    @Range(min = 0, message = "提醒偏移量不能为负数")
    private Integer remindOffset = 1440;  // 默认提前1天
}