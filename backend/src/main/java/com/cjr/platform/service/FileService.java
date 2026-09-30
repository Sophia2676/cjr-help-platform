package com.cjr.platform.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /** 上传图片, 返回访问路径(/uploads/yyyy/MM/xxx.jpg) */
    String upload(MultipartFile file);
}
