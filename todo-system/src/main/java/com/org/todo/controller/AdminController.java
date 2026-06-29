package com.org.todo.controller;

import com.org.todo.dto.PageResult;
import com.org.todo.dto.Result;
import com.org.todo.dto.UserVO;
import com.org.todo.service.StatsService;
import com.org.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StatsService statsService;

    /**
     * 平台整体统计
     */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = statsService.getAdminStats();
        return Result.success(stats);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @RequestParam Integer status) {
        userService.updateStatus(id, status);
        String msg = status == 1 ? "已启用" : "已禁用";
        return Result.success(msg, null);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为 123456", null);
    }

    /**
     * 用户列表（分页）- 管理员
     */
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        PageResult<UserVO> result = userService.getUserList(page, size);
        return Result.success(result);
    }
}