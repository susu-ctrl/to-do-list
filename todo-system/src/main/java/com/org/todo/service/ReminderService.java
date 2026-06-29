package com.org.todo.service;

import com.org.todo.dto.PageResult;
import com.org.todo.entity.ReminderLog;

public interface ReminderService {

    /**
     * 创建提醒记录
     */
    void createReminder(Long taskId, Long userId, String message);

    /**
     * 获取用户未读提醒列表（分页）
     */
    PageResult<ReminderLog> getUnreadReminders(Long userId, Integer page, Integer size);

    /**
     * 获取用户所有提醒列表（分页）
     */
    PageResult<ReminderLog> getReminders(Long userId, Integer page, Integer size);

    /**
     * 获取未读提醒数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记单条提醒为已读
     */
    void markAsRead(Long userId, Long reminderId);

    /**
     * 标记所有提醒为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 扫描并发送到期提醒（定时任务）
     */
    void checkAndSendReminders();

    /**
     * 扫描并标记逾期任务（定时任务）
     */
    void checkAndMarkOverdue();

    /**
     * 将任务加入提醒队列
     */
    void addToReminderQueue(Long taskId, Long dueTime);

    /**
     * 从提醒队列移除任务
     */
    void removeFromReminderQueue(Long taskId);
}