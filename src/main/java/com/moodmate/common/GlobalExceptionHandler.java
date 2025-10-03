package com.moodmate.common;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 일기 없음, 감정 없음
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        if (ex.getMessage().contains("token type") || ex.getMessage().contains("RefreshToken not found")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // 데이터 무결성 위반 (예: Unique 제약 위반)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildErrorResponse("데이터 무결성 오류: 중복 또는 잘못된 입력입니다.", HttpStatus.BAD_REQUEST);
    }

    // 권한 없음 (접근 차단)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse("권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    // 유효성 검증 실패 (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // 기타 예외 (알 수 없는 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        return buildErrorResponse("알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    // GlobalExceptionHandler.java에 추가할 예외 처리 메서드들

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        // 일일 사용량 초과 예외는 429 상태 코드 반환
        if (ex.getMessage().contains("일일 피드백 사용량")) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
        }
        return buildErrorResponse("상태 오류가 발생했습니다.", HttpStatus.BAD_REQUEST);
    }

    // WebClient 관련 예외 처리 (OpenAI API 호출 실패)
    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientException.class)
    public ResponseEntity<Map<String, String>> handleWebClientException(
            org.springframework.web.reactive.function.client.WebClientException ex) {
        return buildErrorResponse("외부 서비스 연결 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    // 타임아웃 예외 처리
    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ResponseEntity<Map<String, String>> handleTimeout(java.util.concurrent.TimeoutException ex) {
        return buildErrorResponse("요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.REQUEST_TIMEOUT);
    }

    // WebClientResponseException 처리 (OpenAI API 응답 오류)
    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientResponseException(
            org.springframework.web.reactive.function.client.WebClientResponseException ex) {

        String message = switch (ex.getStatusCode().value()) {
            case 401 -> "AI 서비스 인증 오류입니다.";
            case 429 -> "AI 서비스 사용량을 초과했습니다. 잠시 후 다시 시도해주세요.";
            case 500, 502, 503 -> "AI 서비스가 일시적으로 이용할 수 없습니다.";
            default -> "AI 서비스 오류가 발생했습니다.";
        };

        return buildErrorResponse(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwt(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Wrong Token."));
    }
}
