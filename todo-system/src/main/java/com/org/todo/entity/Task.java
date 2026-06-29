package com.org.todo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer priority;      // 0-低, 1-中, 2-高
    private Integer status;        // 0-待办, 1-进行中, 2-已完成, 3-已逾期
    private Long categoryId;
    private LocalDateTime dueDate;
    private Integer remindOffset;  // 分钟
    private LocalDateTime completedTime;
    private Integer isDeleted;     // 0-正常, 1-已删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}