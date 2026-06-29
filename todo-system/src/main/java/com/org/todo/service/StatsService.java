package com.org.todo.service;

import java.util.Map;

public interface StatsService {

    /**
     * 获取个人任务统计概览
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取个人效率分析
     */
    Map<String, Object> getUserEfficiency(Long userId);

    /**
     * 获取完成趋势数据（按周/月）
     */
    Map<String, Object> getUserTrends(Long userId, String period);

    /**
     * 获取平台整体统计（管理员）
     */
    Map<String, Object> getAdminStats();
}