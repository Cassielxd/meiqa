package io.renren.crmchat.exception;

import io.renren.crmchat.common.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器 - PHP兼容
 *
 * @author CRMChat Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(CrmChatException.class)
    public ApiResult<Object> handleCrmChatException(CrmChatException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResult.fail(e.getMessage());
    }

    /**
     * 处理数据库唯一键冲突
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<Object> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库唯一键冲突: {}", e.getMessage());
        return ApiResult.fail("Data already exists, please do not repeat the operation");
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("404 Not Found: " + e.getResourcePath());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数验证异常: {}", e.getMessage());
        return ApiResult.fail(e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleException(Exception e) {
        log.error("系统异常: ", e);

        ApiResult<Object> result = new ApiResult<>();
        result.setStatus(500);
        result.setMsg("System busy, please try again later");
        result.setData(null);

        return result;
    }
}
