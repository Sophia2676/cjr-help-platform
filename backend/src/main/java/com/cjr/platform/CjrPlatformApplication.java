package com.cjr.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 残疾人互助交流平台 - 后端启动类
 */
@SpringBootApplication
@MapperScan("com.cjr.platform.mapper")
public class CjrPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CjrPlatformApplication.class, args);
    }
}
