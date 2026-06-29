package com.org.todo.service.impl;

import com.org.todo.dto.CategoryVO;
import com.org.todo.entity.Category;
import com.org.todo.mapper.CategoryMapper;
import com.org.todo.mapper.TaskMapper;
import com.org.todo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public Category createCategory(String name, String color) {
        // 检查分类名是否已存在
        if (categoryMapper.selectByName(name) != null) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setColor(color != null ? color : "#9E9E9E");

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> categories = categoryMapper.selectAll();
        return categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setColor(category.getColor());
            // 统计该分类下的任务数量
            Long count = taskMapper.countTasksByCategoryId(category.getId());
            vo.setTaskCount(count != null ? count : 0L);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateCategory(Long categoryId, String name, String color) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 检查新名称是否已被其他分类使用
        Category existing = categoryMapper.selectByName(name);
        if (existing != null && !existing.getId().equals(categoryId)) {
            throw new RuntimeException("分类名称已存在");
        }

        category.setName(name);
        if (color != null) {
            category.setColor(color);
        }
        categoryMapper.update(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 检查分类下是否有任务
        Long taskCount = taskMapper.countTasksByCategoryId(categoryId);
        if (taskCount != null && taskCount > 0) {
            throw new RuntimeException("该分类下存在 " + taskCount + " 个有效任务，请先删除这些任务后再操作");
        }

        categoryMapper.deleteById(categoryId);
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryMapper.selectById(categoryId);
    }

    @Override
    public Long countTasksByCategoryId(Long categoryId) {
        return taskMapper.countTasksByCategoryId(categoryId);
    }
}