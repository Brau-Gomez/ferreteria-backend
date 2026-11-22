package com.ferreteria.ms_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDTO {
    private boolean success;
    private Object data;
    private String message;
    private String errorCode;
    private Map<String, String> details;
    private Instant timestamp;

    public static ApiErrorDTO of(String errorCode, String message) {
        return ApiErrorDTO.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiErrorDTO of(String errorCode, String message, Map<String, String> details) {
        return ApiErrorDTO.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
}