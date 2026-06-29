package com.org.todo.controller;

import com.org.todo.dto.PageResult;
import com.org.todo.dto.Result;
import com.org.todo.entity.ReminderLog;
import com.org.todo.service.ReminderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    /**
     * 获取未读提醒列表（分页）
     */
    @GetMapping("/unread")
    public Result<PageResult<ReminderLog>> getUnreadReminders(HttpServletRequest request,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<ReminderLog> result = reminderService.getUnreadReminders(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取所有提醒列表（分页）
     */
    @GetMapping
    public Result<PageResult<ReminderLog>> getReminders(HttpServletRequest request,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<ReminderLog> result = reminderService.getReminders(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取未读提醒数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long count = reminderService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        reminderService.markAsRead(userId, id);
        return Result.success("已标记为已读", null);
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reminderService.markAllAsRead(userId);
        return Result.success("已全部标记为已读", null);
    }
}