package com.org.todo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReminderLog {
    private Long id;
    private Long taskId;
    private Long userId;
    private String message;
    private Integer isRead;        // 0-未读, 1-已读
    private LocalDateTime createTime;
}