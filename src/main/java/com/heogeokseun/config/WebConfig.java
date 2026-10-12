
package com.heogeokseun.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 프로필 이미지가 저장되는 실제 폴더
    private final Path profileDirectory;

    // application.properties에서 저장 경로 가져오기
    public WebConfig(
            @Value("${app.upload.profile-dir}") String uploadDir) {

        this.profileDirectory = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // 프로필 이미지 URL과 실제 저장 폴더 연결
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // /uploads/profiles/파일명으로 접근하면
        // 서버의 실제 프로필 이미지 저장 폴더에서 파일을 읽음
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(profileDirectory.toUri().toString());
    }
}
