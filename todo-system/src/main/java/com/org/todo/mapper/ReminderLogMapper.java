package com.org.todo.mapper;

import com.org.todo.entity.ReminderLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReminderLogMapper {

    @Insert("INSERT INTO reminder_log (task_id, user_id, message, create_time) " +
            "VALUES (#{taskId}, #{userId}, #{message}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReminderLog log);

    @Select("SELECT * FROM reminder_log WHERE user_id = #{userId} AND is_read = 0 " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<ReminderLog> selectUnreadByUserId(@Param("userId") Long userId,
                                           @Param("offset") Integer offset,
                                           @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM reminder_log WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(Long userId);

    @Select("SELECT * FROM reminder_log WHERE user_id = #{userId} ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ReminderLog> selectByUserId(@Param("userId") Long userId,
                                     @Param("offset") Integer offset,
                                     @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM reminder_log WHERE user_id = #{userId}")
    Long countByUserId(Long userId);

    @Update("UPDATE reminder_log SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE reminder_log SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);

    @Select("SELECT * FROM reminder_log WHERE task_id = #{taskId} ORDER BY create_time DESC LIMIT 1")
    ReminderLog selectLatestByTaskId(Long taskId);
}