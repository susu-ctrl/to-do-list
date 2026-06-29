package com.org.todo.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer role;
    private Integer status;
    private String token;  // JWT Token
}