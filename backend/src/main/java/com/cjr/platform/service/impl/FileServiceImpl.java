package com.cjr.platform.service.impl;

import com.cjr.platform.common.BusinessException;
import com.cjr.platform.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${cjr.upload.dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择上传文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }

        LocalDate now = LocalDate.now();
        String sub = now.getYear() + "/" + String.format("%02d", now.getMonthValue());
        // UUID 重命名防穿越/覆盖
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(sub);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            throw new BusinessException("图片上传失败，请重试");
        }
        return "/uploads/" + sub + "/" + filename;
    }
}
