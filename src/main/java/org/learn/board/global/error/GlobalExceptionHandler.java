package org.learn.board.global.error;

import lombok.extern.slf4j.Slf4j;
import org.learn.board.global.error.exception.BusinessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검증 실패 시 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handledMethodNotValidException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);

        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // 미지원 Http 메서드 호출 시
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpsRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpsRequestMethodNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);

        return new ResponseEntity<>(response, ErrorCode.METHOD_NOT_ALLOWED.getStatus());
    }

    // 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException e) {
        log.error("handleBusinessException", e);
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // Elasticsearch 연결 실패
    @ExceptionHandler(DataAccessResourceFailureException.class)
    protected ResponseEntity<ErrorResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        log.error("handleDataAccessResourceFailureException: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.SEARCH_SERVICE_UNAVAILABLE);
        return new ResponseEntity<>(response, ErrorCode.SEARCH_SERVICE_UNAVAILABLE.getStatus());
    }

    // 정의되지 않은 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
    }
}
