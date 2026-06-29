package com.org.todo.controller;

import com.org.todo.dto.CategoryVO;
import com.org.todo.dto.Result;
import com.org.todo.entity.Category;
import com.org.todo.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 创建分类（管理员）
     */
    @PostMapping
    public Result<Category> createCategory(@RequestParam String name,
                                           @RequestParam(required = false) String color) {
        Category category = categoryService.createCategory(name, color);
        return Result.success("创建成功", category);
    }

    /**
     * 获取所有分类列表
     */
    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 编辑分类（管理员）
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id,
                                       @RequestParam String name,
                                       @RequestParam(required = false) String color) {
        categoryService.updateCategory(id, name, color);
        return Result.success("更新成功", null);
    }

    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}