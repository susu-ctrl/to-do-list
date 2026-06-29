package com.org.todo.dto;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String color;
    private Long taskCount;  // 该分类下的任务数量
}