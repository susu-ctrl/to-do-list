package com.org.todo.controller;

import com.org.todo.dto.Result;
import com.org.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 修改用户名
     */
    @PutMapping("/username")
    public Result<Void> updateUsername(HttpServletRequest request,
                                       @RequestParam String username) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUsername(userId, username);
        return Result.success("用户名修改成功", null);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只支持图片文件");
        }

        // 校验文件大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过2MB");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件 - 使用绝对路径
            String uploadPath = System.getProperty("user.dir") + "/uploads/avatar/";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();  // 创建目录
            }

            File destFile = new File(uploadPath + filename);
            file.transferTo(destFile);

            // 更新用户头像
            String avatarUrl = "/uploads/avatar/" + filename;
            userService.updateAvatar(userId, avatarUrl);

            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            return Result.success("上传成功", data);

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}