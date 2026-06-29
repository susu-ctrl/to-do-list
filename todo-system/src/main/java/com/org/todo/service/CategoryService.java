package com.org.todo.service;

import com.org.todo.dto.CategoryVO;
import com.org.todo.entity.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 创建分类（管理员）
     */
    Category createCategory(String name, String color);

    /**
     * 获取所有分类列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 更新分类（管理员）
     */
    void updateCategory(Long categoryId, String name, String color);

    /**
     * 删除分类（管理员）
     * 如果分类下有关联任务则抛出异常
     */
    void deleteCategory(Long categoryId);

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Long categoryId);

    /**
     * 检查分类下是否有任务
     */
    Long countTasksByCategoryId(Long categoryId);
}