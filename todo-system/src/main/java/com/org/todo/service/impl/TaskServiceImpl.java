package com.org.todo.service.impl;

import com.org.todo.dto.*;
import com.org.todo.entity.Category;
import com.org.todo.entity.Task;
import com.org.todo.enums.PriorityEnum;
import com.org.todo.enums.TaskStatusEnum;
import com.org.todo.mapper.CategoryMapper;
import com.org.todo.mapper.TaskMapper;
import com.org.todo.service.TaskService;
import com.org.todo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String REMINDER_KEY = "task:reminder:zset";

    @Override
    @Transactional
    public TaskVO createTask(Long userId, TaskCreateRequest request) {
        // 如果传入了分类ID，校验分类是否存在
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("分类不存在");
            }
        }

        // 校验截止日期
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("截止日期不能早于当前时间");
        }

        // 创建任务
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(TaskStatusEnum.PENDING.getCode());
        task.setCategoryId(request.getCategoryId());
        task.setDueDate(request.getDueDate());
        task.setRemindOffset(request.getRemindOffset() != null ? request.getRemindOffset() : 1440);
        task.setIsDeleted(0);

        taskMapper.insert(task);

        // 如果有截止日期且需要提醒，加入Redis提醒队列
        if (task.getDueDate() != null && task.getRemindOffset() > 0) {
            addToReminderQueue(task.getId(), task.getDueDate(), task.getRemindOffset());
        }

        return convertToVO(task);
    }

    @Override
    public PageResult<TaskVO> getTaskList(Long userId, Integer status, Integer page, Integer size) {
        Integer offset = (page - 1) * size;

        List<Task> tasks;
        Long total;

        if (status != null) {
            tasks = taskMapper.selectByUserIdAndStatus(userId, status, offset, size);
            total = taskMapper.countByUserIdAndStatus(userId, status);
        } else {
            tasks = taskMapper.selectByUserId(userId, offset, size);
            total = taskMapper.countByUserId(userId);
        }

        List<TaskVO> voList = tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(total, page, size, voList);
    }

    @Override
    public TaskVO getTaskDetail(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }
        return convertToVO(task);
    }

    @Override
    @Transactional
    public TaskVO updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        // 已完成的任务不可修改
        if (task.getStatus() == TaskStatusEnum.COMPLETED.getCode()) {
            throw new RuntimeException("已完成的任务不可修改");
        }

        // 如果修改了分类ID，校验分类是否存在
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("分类不存在");
            }
        }

        // 更新字段
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getCategoryId() != null) {
            task.setCategoryId(request.getCategoryId());
        }
        if (request.getDueDate() != null) {
            if (request.getDueDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("截止日期不能早于当前时间");
            }
            task.setDueDate(request.getDueDate());
        }
        if (request.getRemindOffset() != null) {
            task.setRemindOffset(request.getRemindOffset());
        }

        // 如果任务已逾期且修改了截止日期，状态回退为待办
        if (task.getStatus() == TaskStatusEnum.OVERDUE.getCode() && request.getDueDate() != null) {
            task.setStatus(TaskStatusEnum.PENDING.getCode());
        }

        taskMapper.update(task);

        // 更新Redis提醒队列
        removeFromReminderQueue(task.getId());
        if (task.getDueDate() != null && task.getRemindOffset() > 0) {
            addToReminderQueue(task.getId(), task.getDueDate(), task.getRemindOffset());
        }

        return convertToVO(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        taskMapper.deleteById(taskId, userId);
        removeFromReminderQueue(taskId);
    }

    @Override
    @Transactional
    public void restoreTask(Long userId, Long taskId) {
        Task task = taskMapper.selectByIdIncludeDeleted(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        if (task.getIsDeleted() != 1) {
            throw new RuntimeException("任务未被删除");
        }

        taskMapper.restoreById(taskId, userId);

        // 如果任务有截止日期且未过期，重新加入提醒队列
        if (task.getDueDate() != null && task.getDueDate().isAfter(LocalDateTime.now()) && task.getRemindOffset() > 0) {
            addToReminderQueue(task.getId(), task.getDueDate(), task.getRemindOffset());
        }
    }

    @Override
    @Transactional
    public void startTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        if (task.getStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new RuntimeException("只有待办状态才能开始任务");
        }

        taskMapper.updateStatus(taskId, userId, TaskStatusEnum.IN_PROGRESS.getCode(), null);
    }

    @Override
    @Transactional
    public void completeTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        if (task.getStatus() != TaskStatusEnum.IN_PROGRESS.getCode()) {
            throw new RuntimeException("只有进行中的任务才能标记完成");
        }

        taskMapper.updateStatus(taskId, userId, TaskStatusEnum.COMPLETED.getCode(), LocalDateTime.now());
        removeFromReminderQueue(taskId);
    }

    @Override
    @Transactional
    public void reopenTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        if (task.getStatus() != TaskStatusEnum.COMPLETED.getCode()) {
            throw new RuntimeException("只有已完成的任务才能重新打开");
        }

        taskMapper.updateStatus(taskId, userId, TaskStatusEnum.PENDING.getCode(), null);

        // 重新加入提醒队列
        if (task.getDueDate() != null && task.getDueDate().isAfter(LocalDateTime.now()) && task.getRemindOffset() > 0) {
            addToReminderQueue(task.getId(), task.getDueDate(), task.getRemindOffset());
        }
    }

    @Override
    @Transactional
    public void completeDirect(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }

        if (task.getStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new RuntimeException("只有待办任务才能直接完成");
        }

        taskMapper.updateStatus(taskId, userId, TaskStatusEnum.COMPLETED.getCode(), LocalDateTime.now());
        removeFromReminderQueue(taskId);
    }

    @Override
    public Task getTaskById(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public List<Task> getUserOverdueTasks(Long userId) {
        return taskMapper.selectOverdueTasks(userId);
    }

    @Override
    public List<Task> getUserTasksWithDueDate(Long userId) {
        return taskMapper.selectAllWithDueDate(userId);
    }

    @Override
    public List<Task> getTasksByCategoryId(Long userId, Long categoryId) {
        return taskMapper.selectByCategoryId(userId, categoryId);
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, Integer status) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            taskMapper.updateStatus(taskId, task.getUserId(), status, null);
        }
    }

    @Override
    public Long getTaskUserId(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        return task != null ? task.getUserId() : null;
    }

    /**
     * 将任务加入Redis提醒队列
     */
    private void addToReminderQueue(Long taskId, LocalDateTime dueDate, Integer remindOffset) {
        LocalDateTime remindTime = dueDate.minusMinutes(remindOffset);
        if (remindTime.isAfter(LocalDateTime.now())) {
            long score = ChronoUnit.SECONDS.between(LocalDateTime.now(), remindTime);
            redisUtils.zAdd(REMINDER_KEY, String.valueOf(taskId), score);
        }
    }

    /**
     * 从Redis提醒队列移除任务
     */
    private void removeFromReminderQueue(Long taskId) {
        redisUtils.zRemove(REMINDER_KEY, String.valueOf(taskId));
    }

    /**
     * 将Task实体转换为TaskVO
     */
    private TaskVO convertToVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setPriority(task.getPriority());
        vo.setStatus(task.getStatus());
        vo.setCategoryId(task.getCategoryId());
        vo.setDueDate(task.getDueDate());
        vo.setRemindOffset(task.getRemindOffset());
        vo.setCompletedTime(task.getCompletedTime());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        // 优先级文本
        for (PriorityEnum p : PriorityEnum.values()) {
            if (p.getCode().equals(task.getPriority())) {
                vo.setPriorityText(p.getDesc());
                break;
            }
        }

        // 状态文本
        for (TaskStatusEnum s : TaskStatusEnum.values()) {
            if (s.getCode().equals(task.getStatus())) {
                vo.setStatusText(s.getDesc());
                break;
            }
        }

        // 分类信息
        if (task.getCategoryId() != null) {
            Category category = categoryMapper.selectById(task.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryColor(category.getColor());
            }
        }

        // 逾期判断
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()) &&
                task.getStatus() != TaskStatusEnum.COMPLETED.getCode()) {
            vo.setIsOverdue(true);
        } else {
            vo.setIsOverdue(false);
        }

        return vo;
    }
}