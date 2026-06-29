package com.org.todo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskVO {
    private Long id;
    private String title;
    private String description;
    private Integer priority;
    private String priorityText;  // 低/中/高
    private Integer status;
    private String statusText;    // 待办/进行中/已完成/已逾期
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private LocalDateTime dueDate;
    private Integer remindOffset;
    private LocalDateTime completedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isOverdue;    // 是否逾期（前端高亮用）
}