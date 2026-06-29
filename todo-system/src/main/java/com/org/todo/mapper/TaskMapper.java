package com.org.todo.mapper;

import com.org.todo.entity.Task;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskMapper {

    @Insert("INSERT INTO task (user_id, title, description, priority, status, category_id, due_date, remind_offset, create_time, update_time) " +
            "VALUES (#{userId}, #{title}, #{description}, #{priority}, #{status}, #{categoryId}, #{dueDate}, #{remindOffset}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    @Select("SELECT * FROM task WHERE id = #{id} AND is_deleted = 0")
    Task selectById(Long id);

    @Select("SELECT * FROM task WHERE id = #{id}")
    Task selectByIdIncludeDeleted(Long id);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND is_deleted = 0 " +
            "ORDER BY due_date ASC, priority DESC " +
            "LIMIT #{offset}, #{size}")
    List<Task> selectByUserId(@Param("userId") Long userId,
                              @Param("offset") Integer offset,
                              @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND is_deleted = 0")
    Long countByUserId(Long userId);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND status = #{status} AND is_deleted = 0 " +
            "ORDER BY due_date ASC, priority DESC " +
            "LIMIT #{offset}, #{size}")
    List<Task> selectByUserIdAndStatus(@Param("userId") Long userId,
                                       @Param("status") Integer status,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND status = #{status} AND is_deleted = 0")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Update("UPDATE task SET title = #{title}, description = #{description}, priority = #{priority}, " +
            "category_id = #{categoryId}, due_date = #{dueDate}, remind_offset = #{remindOffset}, " +
            "update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int update(Task task);

    @Update("UPDATE task SET status = #{status}, completed_time = #{completedTime}, update_time = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("status") Integer status,
                     @Param("completedTime") LocalDateTime completedTime);

    @Update("UPDATE task SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE task SET is_deleted = 0, status = 0, update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int restoreById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND is_deleted = 0 AND due_date IS NOT NULL " +
            "AND status IN (0, 1) AND due_date < NOW()")
    List<Task> selectOverdueTasks(Long userId);

    /**
     * 查询所有用户的逾期任务（不限制用户ID）
     */
    @Select("SELECT * FROM task WHERE is_deleted = 0 AND due_date IS NOT NULL " +
            "AND status IN (0, 1) AND due_date < NOW()")
    List<Task> selectAllOverdueTasks();

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND is_deleted = 0 AND due_date IS NOT NULL")
    List<Task> selectAllWithDueDate(Long userId);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND is_deleted = 0 AND category_id = #{categoryId}")
    List<Task> selectByCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Select("SELECT COUNT(*) FROM task WHERE is_deleted = 0")
    Long countTotalAll();

    @Select("SELECT COUNT(*) FROM task WHERE is_deleted = 0 AND status = #{status}")
    Long countByStatusAll(Integer status);

    @Select("SELECT COUNT(*) FROM task WHERE is_deleted = 0 AND user_id = #{userId} AND status = #{status}")
    Long countByUserIdAndStatusAll(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM task WHERE is_deleted = 0 AND DATE(create_time) = CURDATE()")
    Long countTodayNewTasks();

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND is_deleted = 0 AND status = 2 AND DATE(completed_time) = CURDATE()")
    Long countTodayCompletedTasks(Long userId);

    /**
     * 统计指定分类下的有效任务数量
     */
    @Select("SELECT COUNT(*) FROM task WHERE category_id = #{categoryId} AND is_deleted = 0")
    Long countTasksByCategoryId(Long categoryId);
}