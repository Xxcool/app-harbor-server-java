package com.example.apppublish.common;

/** 前后端统一响应结构。 */
public record ApiResponse<T>(String code, String msg, T data) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>("0000", "success", data); }
  public static ApiResponse<Void> ok() { return ok(null); }
  public static ApiResponse<Void> fail(String code, String message) { return new ApiResponse<>(code, message, null); }
}
