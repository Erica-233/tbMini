package com.example.tbmini.service;

import com.example.tbmini.domain.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 * 处理图片文件的上传和存储
 */
@Service
public class UploadService {
    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 保存多张图片
     * @param files 图片文件列表
     * @return 保存后的URL列表
     */
    public List<String> saveImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream().map(this::saveSingle).toList();
    }

    /**
     * 保存单张图片
     * @param file 图片文件
     * @return 保存后的URL
     */
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return saveSingle(file);
    }

    /**
     * 保存单张图片的内部方法
     * @param file 图片文件
     * @return 保存后的URL
     */
    private String saveSingle(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException("文件大小超过5MB限制");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BizException("仅支持 jpg/png/webp 格式的图片");
        }

        try {
            String subDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path dir = Paths.get(uploadDir, subDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String ext = switch (contentType.toLowerCase()) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> "";
            };
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new BizException("文件上传失败");
        }
    }
}