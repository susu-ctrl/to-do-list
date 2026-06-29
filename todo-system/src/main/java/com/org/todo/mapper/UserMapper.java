package com.org.todo.mapper;

import com.org.todo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user (username, password, role, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{role}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM user WHERE id = #{id} AND status = 1")
    User selectActiveById(Long id);

    @Update("UPDATE user SET username = #{username}, update_time = NOW() WHERE id = #{id}")
    int updateUsername(@Param("id") Long id, @Param("username") String username);

    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET avatar_url = #{avatarUrl}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE user SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByUsername(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM user")
    Long countTotal();

    @Select("SELECT COUNT(*) FROM user WHERE role = 0")
    Long countNormalUsers();

    @Select("SELECT COUNT(*) FROM user WHERE status = 0")
    Long countDisabledUsers();

    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()")
    Long countTodayNewUsers();

    /**
     * 分页查询所有用户
     */
    @Select("SELECT * FROM user ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<User> selectAll(Integer offset, Integer size);
}