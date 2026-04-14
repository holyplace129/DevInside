package org.learn.board.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버에 오류가 발생했습니다."),

    // Post Errors
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 게시글을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "P002", "비밀번호가 일치하지 않습니다."),

    // Gallery Errors
    GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "해당 갤러리를 찾을 수 없습니다."),
    GALLERY_NAME_DUPLICATED(HttpStatus.CONFLICT, "G002", "이미 존재하는 갤러리 이름입니다."),

    // Comment Errors
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "해당 댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM002", "부모 댓글을 찾을 수 없습니다."),

    // Vote & Report Errors
    ALREADY_VOTED(HttpStatus.CONFLICT, "V001", "이미 추천/비추천한 게시물입니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "R001", "이미 신고한 게시물입니다."),

    // External Service Errors
    SEARCH_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "S001", "검색 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
