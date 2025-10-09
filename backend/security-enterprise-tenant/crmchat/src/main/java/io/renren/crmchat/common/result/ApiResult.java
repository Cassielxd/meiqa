package io.renren.crmchat.common.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * API统一响应格式 (兼容PHP格式)
 * PHP: {"status": 200, "msg": "ok", "data": {...}}
 *
 * @author CRMChat
 */
@Data
public class ApiResult<T> {

    /**
     * 状态码: 200成功, 400客户端错误, 500服务器错误
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 提示消息
     */
    @JsonProperty("msg")
    private String msg;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;

    public ApiResult() {
    }

    public ApiResult(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应(无数据)
     */
    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(200, "ok", null);
    }

    /**
     * 成功响应(带数据)
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "ok", data);
    }

    /**
     * 成功响应(自定义消息)
     */
    public static <T> ApiResult<T> ok(String msg, T data) {
        return new ApiResult<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(400, msg, null);
    }

    /**
     * 失败响应(自定义状态码)
     */
    public static <T> ApiResult<T> fail(Integer status, String msg) {
        return new ApiResult<>(status, msg, null);
    }

    /**
     * 服务器错误响应
     */
    public static <T> ApiResult<T> error(String msg) {
        return new ApiResult<>(500, msg, null);
    }
}
