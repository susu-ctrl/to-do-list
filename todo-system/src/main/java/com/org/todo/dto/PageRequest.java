package com.org.todo.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;

@Data
public class PageRequest {

    @Min(value = 1, message = "页码从1开始")
    private Integer page = 1;

    @Min(value = 1, message = "每页大小至少为1")
    private Integer size = 10;

    public Integer getOffset() {
        return (page - 1) * size;
    }
}