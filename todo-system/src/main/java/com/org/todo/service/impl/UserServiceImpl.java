package com.org.todo.service.impl;

import com.org.todo.dto.LoginRequest;
import com.org.todo.dto.PageResult;
import com.org.todo.dto.RegisterRequest;
import com.org.todo.dto.UserVO;
import com.org.todo.entity.User;
import com.org.todo.enums.UserRoleEnum;
import com.org.todo.mapper.UserMapper;
import com.org.todo.service.UserService;
import com.org.todo.utils.JwtUtils;
import com.org.todo.utils.PasswordUtils;
import com.org.todo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    private static final String TOKEN_PREFIX = "token:";
    private static final Long TOKEN_EXPIRE_TIME = 24L * 60 * 60; // 24小时

    @Override
    @Transactional
    public UserVO register(RegisterRequest request) {
        // 校验用户名是否已存在
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordUtils.encode(request.getPassword()));
        user.setRole(UserRoleEnum.USER.getCode());
        user.setStatus(1); // 正常状态

        userMapper.insert(user);

        // 返回用户信息
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }

    @Override
    public UserVO login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        if (!passwordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        // 将Token存入Redis
        String key = TOKEN_PREFIX + user.getId();
        redisUtils.set(key, token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        // 返回用户信息
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setToken(token);
        return vo;
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public void updateUsername(Long userId, String username) {
        // 检查新用户名是否已被其他用户使用
        User existing = userMapper.selectByUsername(username);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new RuntimeException("用户名已被占用");
        }
        userMapper.updateUsername(userId, username);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordUtils.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        userMapper.updatePassword(userId, passwordUtils.encode(newPassword));

        // 清除Redis中的Token（强制重新登录）
        redisUtils.delete(TOKEN_PREFIX + userId);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getRole() == UserRoleEnum.ADMIN.getCode()) {
            throw new RuntimeException("不能禁用管理员账号");
        }
        userMapper.updateStatus(userId, status);

        // 如果禁用，清除Redis中的Token
        if (status == 0) {
            redisUtils.delete(TOKEN_PREFIX + userId);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 重置为默认密码：123456
        userMapper.updatePassword(userId, passwordUtils.encode("123456"));
        // 清除Redis中的Token
        redisUtils.delete(TOKEN_PREFIX + userId);
    }

    @Override
    public boolean checkUsername(String username) {
        return userMapper.selectByUsername(username) == null;
    }

    @Override
    public void logout(Long userId, String token) {
        // 删除Redis中的Token
        redisUtils.delete(TOKEN_PREFIX + userId);
    }

    @Override
    public PageResult<UserVO> getUserList(Integer page, Integer size) {
        Integer offset = (page - 1) * size;

        // 查询用户列表
        List<User> users = userMapper.selectAll(offset, size);
        Long total = userMapper.countTotal();

        // 转换为VO
        List<UserVO> voList = users.stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(total, page, size, voList);
    }
}