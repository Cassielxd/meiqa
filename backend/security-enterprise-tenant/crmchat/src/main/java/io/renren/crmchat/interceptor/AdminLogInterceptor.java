package io.renren.crmchat.interceptor;

import io.renren.crmchat.dao.SystemLogMapper;
import io.renren.crmchat.entity.SystemLogEntity;
import io.renren.crmchat.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员操作日志拦截器
 * PHP Reference: /app/http/middleware/admin/AdminLogMiddleware.php
 *
 * 功能说明:
 * - 记录所有管理员操作日志
 * - 在每个请求完成后自动记录
 * - 记录内容: 管理员ID、账号、访问路径、IP地址、操作时间
 *
 * PHP逻辑:
 * 1. 中间件在每个请求时触发
 * 2. 调用 SystemLogServices::recordAdminLog()
 * 3. 保存到 eb_system_log 表
 *
 * @author CRMChat Team
 */
@Slf4j
@Component
@AllArgsConstructor
public class AdminLogInterceptor implements HandlerInterceptor {

    private final SystemLogMapper systemLogMapper;

    /**
     * 请求完成后记录日志
     * PHP Reference: AdminLogMiddleware::handle() + SystemLogServices::recordAdminLog()
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param ex 异常(如果有)
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        try {
            // 只记录管理员 API 操作 (/api/admin/**)
            String path = request.getRequestURI();

            log.info("=== AdminLogInterceptor triggered === path: {}", path);

            if (!path.startsWith("/api/admin/")) {
                log.info("=== Skipping non-admin API: {}", path);
                return;
            }

            // 排除登录接口,避免重复记录
            if (path.contains("/login")) {
                return;
            }

            // 获取当前登录管理员信息
            // PHP: $request->adminId(), $request->adminInfo()['account']
            Long userId = UserContext.getUserId();
            String adminName = UserContext.getUsername();

            if (userId == null || adminName == null) {
                return; // 未登录或获取失败,不记录
            }

            Integer adminId = userId.intValue();

            // PHP: $module = app('http')->getName(); // 'system'
            String method = "admin";

            // PHP: $rule = trim(strtolower($request->rule()->getRule()));
            // 请求路径,如: /api/admin/setting/group

            // PHP: $service->getVisitName($rule) ?: '未知'
            // 这里简化处理,使用请求方法 + 路径
            String page = request.getMethod() + " " + path;

            // PHP: $request->ip()
            String ip = getClientIp(request);

            // PHP: $type = $type; // 'system'
            String type = "system";

            // 创建日志记录
            // PHP: $data = ['method' => ..., 'admin_id' => ..., ...]
            SystemLogEntity logEntity = new SystemLogEntity();
            logEntity.setAdminId(adminId);
            logEntity.setAdminName(adminName);
            logEntity.setMethod(method);
            logEntity.setPath(path);
            logEntity.setPage(page);
            logEntity.setIp(ip);
            logEntity.setType(type);
            logEntity.setAddTime(new java.sql.Timestamp(System.currentTimeMillis())); // 当前时间戳

            // PHP: $this->dao->save($data)
            int result = systemLogMapper.insert(logEntity);

            log.info("=== Logged admin action successfully === adminId={}, path={}, ip={}, result={}", adminId, path, ip, result);

        } catch (Exception e) {
            // PHP: catch (\Throwable $e) {} - 静默失败,不影响业务
            log.error("Failed to record admin action log", e);
        }
    }

    /**
     * 获取客户端真实IP地址
     * PHP: $request->ip()
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值,第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
