package com.org.todo.mapper;

import com.org.todo.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Insert("INSERT INTO category (name, color, create_time, update_time) " +
            "VALUES (#{name}, #{color}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Select("SELECT * FROM category ORDER BY name")
    List<Category> selectAll();

    @Select("SELECT * FROM category WHERE name = #{name}")
    Category selectByName(String name);

    @Update("UPDATE category SET name = #{name}, color = #{color}, update_time = NOW() WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM category")
    Long countTotal();

    @Select("SELECT COUNT(*) FROM task WHERE category_id = #{categoryId} AND is_deleted = 0")
    Long countTasksByCategoryId(Long categoryId);
}