package io.renren.modules.kefu.vo;

import lombok.Data;

/**
 * 统一API响应格式 (兼容PHP格式)
 * PHP格式: {"status": 200, "msg": "ok", "data": {...}}
 */
@Data
public class ApiResponse<T> {
    /**
     * 状态码 (200成功, 400/500失败)
     */
    private Integer status;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMsg("ok");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> fail(String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMsg(msg);
        return response;
    }

    public static <T> ApiResponse<T> fail(Integer status, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(status);
        response.setMsg(msg);
        return response;
    }
}
