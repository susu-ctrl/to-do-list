package com.org.todo.service.impl;

import com.org.todo.dto.PageResult;
import com.org.todo.entity.ReminderLog;
import com.org.todo.entity.Task;
import com.org.todo.enums.TaskStatusEnum;
import com.org.todo.mapper.ReminderLogMapper;
import com.org.todo.mapper.TaskMapper;
import com.org.todo.service.ReminderService;
import com.org.todo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ReminderServiceImpl implements ReminderService {

    @Autowired
    private ReminderLogMapper reminderLogMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String REMINDER_KEY = "task:reminder:zset";
    private static final String SENT_KEY_PREFIX = "reminder:sent:";

    @Override
    @Transactional
    public void createReminder(Long taskId, Long userId, String message) {
        ReminderLog log = new ReminderLog();
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setMessage(message);
        log.setIsRead(0);
        reminderLogMapper.insert(log);
    }

    @Override
    public PageResult<ReminderLog> getUnreadReminders(Long userId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        List<ReminderLog> list = reminderLogMapper.selectUnreadByUserId(userId, offset, size);
        Long total = reminderLogMapper.countUnreadByUserId(userId);
        return new PageResult<>(total, page, size, list);
    }

    @Override
    public PageResult<ReminderLog> getReminders(Long userId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        List<ReminderLog> list = reminderLogMapper.selectByUserId(userId, offset, size);
        Long total = reminderLogMapper.countByUserId(userId);
        return new PageResult<>(total, page, size, list);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return reminderLogMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long reminderId) {
        reminderLogMapper.markAsRead(reminderId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        reminderLogMapper.markAllAsRead(userId);
    }

    @Override
    @Scheduled(fixedDelay = 1800000) // 每30分钟执行一次
    public void checkAndSendReminders() {
        long now = System.currentTimeMillis() / 1000; // 转换为秒

        // 获取所有已到提醒时间的任务ID
        Set<String> taskIds = redisUtils.zRangeByScore(REMINDER_KEY, 0, now);

        for (String taskIdStr : taskIds) {
            Long taskId = Long.parseLong(taskIdStr);

            // 检查是否已发送过提醒
            String sentKey = SENT_KEY_PREFIX + taskId;
            if (redisUtils.hasKey(sentKey)) {
                continue;
            }

            // 获取任务信息
            Task task = taskMapper.selectById(taskId);
            if (task == null || task.getIsDeleted() == 1) {
                // 任务已删除，从队列移除
                removeFromReminderQueue(taskId);
                continue;
            }

            // 已完成的任务不再提醒
            if (task.getStatus() == TaskStatusEnum.COMPLETED.getCode()) {
                removeFromReminderQueue(taskId);
                continue;
            }

            // 发送提醒
            String message = String.format("任务「%s」即将到期，截止日期：%s",
                    task.getTitle(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "未知");
            createReminder(taskId, task.getUserId(), message);

            // 标记已提醒（7天过期）
            redisUtils.set(sentKey, "1", 7, TimeUnit.DAYS);

            // 从队列移除
            removeFromReminderQueue(taskId);
        }
    }

    @Override
    @Scheduled(fixedDelay = 30000) // 每天凌晨1点执行
    @Transactional
    public void checkAndMarkOverdue() {
        // 获取所有用户的所有逾期任务
        // 注意：这里需要查询所有用户，实际项目中可以优化为分批查询
        System.out.println("========== 逾期检查定时任务执行 ==========");
        List<Task> tasks = taskMapper.selectAllOverdueTasks();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatusEnum.PENDING.getCode() ||
                    task.getStatus() == TaskStatusEnum.IN_PROGRESS.getCode()) {
                // 更新状态为已逾期
                taskMapper.updateStatus(task.getId(), task.getUserId(),
                        TaskStatusEnum.OVERDUE.getCode(), null);
            }
        }
    }

    @Override
    public void addToReminderQueue(Long taskId, Long dueTime) {
        if (dueTime > 0) {
            redisUtils.zAdd(REMINDER_KEY, String.valueOf(taskId), dueTime);
        }
    }

    @Override
    public void removeFromReminderQueue(Long taskId) {
        redisUtils.zRemove(REMINDER_KEY, String.valueOf(taskId));
        redisUtils.delete(SENT_KEY_PREFIX + taskId);
    }
}