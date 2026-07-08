package com.flowforge.common;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> failed(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}