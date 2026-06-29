package com.org.todo.controller;

import com.org.todo.dto.*;
import com.org.todo.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    public Result<TaskVO> createTask(HttpServletRequest request,
                                     @Valid @RequestBody TaskCreateRequest createRequest) {
        Long userId = (Long) request.getAttribute("userId");
        TaskVO task = taskService.createTask(userId, createRequest);
        return Result.success("创建成功", task);
    }

    /**
     * 获取任务列表（分页）
     */
    @GetMapping
    public Result<PageResult<TaskVO>> getTaskList(HttpServletRequest request,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<TaskVO> result = taskService.getTaskList(userId, status, page, size);
        return Result.success(result);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<TaskVO> getTaskDetail(HttpServletRequest request,
                                        @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        TaskVO task = taskService.getTaskDetail(userId, id);
        return Result.success(task);
    }

    /**
     * 编辑任务
     */
    @PutMapping("/{id}")
    public Result<TaskVO> updateTask(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @Valid @RequestBody TaskUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        TaskVO task = taskService.updateTask(userId, id, updateRequest);
        return Result.success("更新成功", task);
    }

    /**
     * 逻辑删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.deleteTask(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 恢复任务（回收站 → 待办）
     */
    @PutMapping("/{id}/restore")
    public Result<Void> restoreTask(HttpServletRequest request,
                                    @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.restoreTask(userId, id);
        return Result.success("恢复成功", null);
    }

    /**
     * 开始任务（待办 → 进行中）
     */
    @PutMapping("/{id}/start")
    public Result<Void> startTask(HttpServletRequest request,
                                  @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.startTask(userId, id);
        return Result.success("任务已开始", null);
    }

    /**
     * 标记完成（进行中 → 已完成）
     */
    @PutMapping("/{id}/complete")
    public Result<Void> completeTask(HttpServletRequest request,
                                     @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.completeTask(userId, id);
        return Result.success("任务已完成", null);
    }

    /**
     * 重新打开（已完成 → 待办）
     */
    @PutMapping("/{id}/reopen")
    public Result<Void> reopenTask(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.reopenTask(userId, id);
        return Result.success("任务已重新打开", null);
    }

    /**
     * 直接完成（待办 → 已完成）
     */
    @PutMapping("/{id}/complete-direct")
    public Result<Void> completeDirect(HttpServletRequest request,
                                       @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.completeDirect(userId, id);
        return Result.success("任务已完成", null);
    }
}