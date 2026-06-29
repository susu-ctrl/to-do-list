package com.org.todo.controller;

import com.org.todo.dto.Result;
import com.org.todo.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 个人任务统计概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> stats = statsService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 个人效率分析
     */
    @GetMapping("/efficiency")
    public Result<Map<String, Object>> getUserEfficiency(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> efficiency = statsService.getUserEfficiency(userId);
        return Result.success(efficiency);
    }

    /**
     * 完成趋势数据（周/月）
     */
    @GetMapping("/trends")
    public Result<Map<String, Object>> getUserTrends(HttpServletRequest request,
                                                     @RequestParam(required = false) String period) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> trends = statsService.getUserTrends(userId, period);
        return Result.success(trends);
    }
}