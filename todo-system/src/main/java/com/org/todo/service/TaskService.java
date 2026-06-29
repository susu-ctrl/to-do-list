package com.org.todo.service;

import com.org.todo.dto.PageResult;
import com.org.todo.dto.TaskCreateRequest;
import com.org.todo.dto.TaskUpdateRequest;
import com.org.todo.dto.TaskVO;
import com.org.todo.entity.Task;

import java.util.List;

public interface TaskService {

    /**
     * 创建任务
     */
    TaskVO createTask(Long userId, TaskCreateRequest request);

    /**
     * 获取任务列表（分页）
     */
    PageResult<TaskVO> getTaskList(Long userId, Integer status, Integer page, Integer size);

    /**
     * 获取任务详情
     */
    TaskVO getTaskDetail(Long userId, Long taskId);

    /**
     * 编辑任务
     */
    TaskVO updateTask(Long userId, Long taskId, TaskUpdateRequest request);

    /**
     * 逻辑删除任务
     */
    void deleteTask(Long userId, Long taskId);

    /**
     * 恢复任务
     */
    void restoreTask(Long userId, Long taskId);

    /**
     * 开始任务（待办 → 进行中）
     */
    void startTask(Long userId, Long taskId);

    /**
     * 标记完成（进行中 → 已完成）
     */
    void completeTask(Long userId, Long taskId);

    /**
     * 重新打开（已完成 → 待办）
     */
    void reopenTask(Long userId, Long taskId);

    /**
     * 直接完成（待办 → 已完成）
     */
    void completeDirect(Long userId, Long taskId);

    /**
     * 获取任务实体（用于提醒服务）
     */
    Task getTaskById(Long taskId);

    /**
     * 获取用户所有未完成且有过期时间的任务（用于逾期标记）
     */
    List<Task> getUserOverdueTasks(Long userId);

    /**
     * 获取用户所有有截止日期的任务（用于统计）
     */
    List<Task> getUserTasksWithDueDate(Long userId);

    /**
     * 获取用户指定分类下的所有任务（用于分类删除校验）
     */
    List<Task> getTasksByCategoryId(Long userId, Long categoryId);

    /**
     * 更新任务状态（用于逾期标记）
     */
    void updateTaskStatus(Long taskId, Integer status);

    /**
     * 获取任务所属用户ID
     */
    Long getTaskUserId(Long taskId);
}