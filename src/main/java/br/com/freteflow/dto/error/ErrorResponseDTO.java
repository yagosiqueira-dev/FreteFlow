package br.com.freteflow.dto.error;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponseDTO of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, details);
    }
}