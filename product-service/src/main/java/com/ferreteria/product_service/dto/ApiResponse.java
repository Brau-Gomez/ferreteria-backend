package com.ferreteria.product_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder().success(true).data(data).message(message).timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder().success(false).errorCode(errorCode).message(message).timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return ApiResponse.<T>builder().success(false).data(data).errorCode(errorCode).message(message).timestamp(Instant.now()).build();
    }
}