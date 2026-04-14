package org.learn.board.domain.image.controller;

import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.InvalidValueException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageFacade {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${custom.upload.path}")
    private String uploadPath;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 1. 원본 파일명 및 확장자 추출
        String originFilename = file.getOriginalFilename();
        if (originFilename == null || !originFilename.contains(".")) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 2. 확장자 검증 (허용된 이미지 확장자만 가능)
        String fileExtension = originFilename.substring(originFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 3. MIME 타입 검증
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 4. 고유한 파일명 생성(UUID 사용)
        String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;

        // 5. 디렉토리 트래버설 방지 후 파일 저장
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File destFile = new File(uploadDir, storedFilename);
        if (!destFile.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }
        file.transferTo(destFile);

        // 6. 클라이언트가 접근할 url 반환
        return "/images/" + storedFilename;
    }
}
