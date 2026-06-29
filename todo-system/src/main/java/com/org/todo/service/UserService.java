package com.org.todo.service;

import com.org.todo.dto.LoginRequest;
import com.org.todo.dto.PageResult;
import com.org.todo.dto.RegisterRequest;
import com.org.todo.dto.UserVO;
import com.org.todo.entity.User;

public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(RegisterRequest request);

    /**
     * 用户登录
     */
    UserVO login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    User getCurrentUser(Long userId);

    /**
     * 修改用户名
     */
    void updateUsername(Long userId, String username);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改头像
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 启用/禁用用户（管理员功能）
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 重置密码（管理员功能）
     */
    void resetPassword(Long userId);

    /**
     * 检查用户名是否可用
     */
    boolean checkUsername(String username);

    /**
     * 退出登录
     */
    void logout(Long userId, String token);

    PageResult<UserVO> getUserList(Integer page, Integer size);
}