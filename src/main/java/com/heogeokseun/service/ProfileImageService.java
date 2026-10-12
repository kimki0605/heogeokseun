
package com.heogeokseun.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageService {

    // 프로필 이미지 저장 폴더
    private final Path profileDirectory;

    // 최대 업로드 크기: 5MB
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    // 이미지 최대 가로 및 세로 크기
    private static final int MAX_IMAGE_DIMENSION = 5000;

    // 허용할 이미지 형식
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png");

    public ProfileImageService(
            @Value("${app.upload.profile-dir}") String uploadDir) {

        this.profileDirectory = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // ========================================
    // 프로필 이미지 저장
    // ========================================
    public String saveProfileImage(MultipartFile file) throws IOException {

        // 파일 선택 여부 확인
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "프로필 사진을 선택해주세요.");
        }

        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "프로필 사진은 최대 5MB까지 업로드할 수 있습니다.");
        }

        // 브라우저가 전달한 Content-Type 확인
        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(contentType)) {

            throw new IllegalArgumentException(
                    "JPG 또는 PNG 이미지만 업로드할 수 있습니다.");
        }

        // 실제 파일 내용을 검사하여 이미지 형식 확인
        String actualFormat;

        try (InputStream inputStream = file.getInputStream();
             ImageInputStream imageInputStream =
                     ImageIO.createImageInputStream(inputStream)) {

            if (imageInputStream == null) {
                throw new IllegalArgumentException(
                        "이미지 파일을 읽을 수 없습니다.");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInputStream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "올바른 JPG 또는 PNG 이미지가 아닙니다.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInputStream);

                // 실제 이미지 형식 확인
                actualFormat = reader.getFormatName()
                        .toLowerCase();

                // 이미지 크기 확인
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION) {

                    throw new IllegalArgumentException(
                            "이미지 크기가 너무 큽니다. "
                            + "가로와 세로는 최대 5000픽셀까지 가능합니다.");
                }

                // 실제 이미지 디코딩 검사
                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "올바른 이미지 파일이 아닙니다.");
                }

            } finally {
                reader.dispose();
            }
        }

        // 실제 형식과 Content-Type이 일치하는지 확인
        String extension;

        if ("jpeg".equals(actualFormat)
                || "jpg".equals(actualFormat)) {

            if (!"image/jpeg".equals(contentType)) {
                throw new IllegalArgumentException(
                        "파일의 실제 형식과 확장자가 일치하지 않습니다.");
            }

            extension = ".jpg";

        } else if ("png".equals(actualFormat)) {

            if (!"image/png".equals(contentType)) {
                throw new IllegalArgumentException(
                        "파일의 실제 형식과 확장자가 일치하지 않습니다.");
            }

            extension = ".png";

        } else {
            throw new IllegalArgumentException(
                    "JPG 또는 PNG 이미지만 업로드할 수 있습니다.");
        }

        // UUID를 이용한 고유 파일명 생성
        String fileName = UUID.randomUUID() + extension;

        // 저장 폴더가 없으면 자동 생성
        Files.createDirectories(profileDirectory);

        // 실제 저장 경로
        Path targetPath = profileDirectory
                .resolve(fileName)
                .normalize();

        // 폴더 밖으로 저장되는 것을 방지
        if (!targetPath.startsWith(profileDirectory)) {
            throw new IllegalArgumentException(
                    "잘못된 파일 경로입니다.");
        }

        // 이미지 파일 저장
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath);
        }

        // DB에 저장할 URL 반환
        return "/uploads/profiles/" + fileName;
    }

    // ========================================
    // 기존 프로필 이미지 삭제
    // ========================================
    public void deleteProfileImage(String imageUrl) throws IOException {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/profiles/";

        // 서비스가 생성한 URL인지 확인
        if (!imageUrl.startsWith(prefix)) {
            return;
        }

        String fileName = imageUrl.substring(prefix.length());

        // UUID 파일명과 허용 확장자 확인
        if (!fileName.matches(
                "[0-9a-fA-F]{8}-"
                + "[0-9a-fA-F]{4}-"
                + "[0-9a-fA-F]{4}-"
                + "[0-9a-fA-F]{4}-"
                + "[0-9a-fA-F]{12}\\.(jpg|png|webp)")) {
            return;
        }

        Path targetPath = profileDirectory
                .resolve(fileName)
                .normalize();

        if (!targetPath.startsWith(profileDirectory)) {
            return;
        }

        // 파일이 존재할 때만 삭제
        Files.deleteIfExists(targetPath);
    }
}
