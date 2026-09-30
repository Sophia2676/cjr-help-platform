package com.cjr.platform.config;

import com.cjr.platform.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置: 拦截器注册 / CORS / 上传目录静态映射
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${cjr.upload.dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Windows 下必须为绝对路径且以 / 结尾, 否则解析错目录
        String path = Paths.get(uploadDir).toAbsolutePath().normalize().toString().replace('\\', '/') + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + path);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Vite 代理为主, 此处兜底
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
