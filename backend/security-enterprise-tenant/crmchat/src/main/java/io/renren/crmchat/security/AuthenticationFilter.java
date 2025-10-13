package io.renren.crmchat.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.crmchat.common.constant.ApiConstants;
import io.renren.crmchat.common.constant.TenantStatus;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.dao.SystemAdminMapper;
import io.renren.crmchat.dao.TenantsMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.entity.TenantsEntity;
import io.renren.crmchat.service.AdminApplicationService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Map;

/**
 * JWT认证过滤器
 * 处理PHP兼容的 Authori-zation 请求头
 *
 * @author CRMChat Team
 */
@Slf4j
@Component
@AllArgsConstructor
public class AuthenticationFilter implements Filter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final SystemAdminMapper systemAdminMapper;
    private final TenantsMapper tenantsMapper;
    private final AdminApplicationService adminApplicationService;

    /**
     * 不需要认证的URL
     */
    private static final String[] EXCLUDE_URL_PATTERNS = {
            "/api/admin/login",
            "/api/admin/login/info",
            "/api/admin/reset_admin_password",
            "/api/admin/fix_tenant_max_services",
            "/api/admin/ajcaptcha",
            "/api/admin/ajcheck",
            "/api/tenant/login",
            "/api/tenant/register",
            "/api/tenant/send_captcha",
            "/api/tenant/captcha_pro",
            "/api/tenant/login/info",
            "/api/tenant/ajcaptcha",
            "/api/tenant/ajcheck",
            "/api/tenant/tourist/**",  // 游客访问接口（不需要认证）
            "/api/tenant/user/record",  // 游客聊天记录
            "/api/tenant/user/statistics",  // 游客访问统计
            "/api/tenant/service/adv",  // 客服页面广告
            "/api/kefu/login",
            "/api/kefu/config",
            "/api/kefu/ajcaptcha",
            "/api/kefu/ajcheck",
            "/api/mobile/login",
            "/api/mobile/service/auto_login",  // 游客自动登录
            "/api/mobile/service/icon",  // 获取客服图标配置（租户网站集成时需要）
            "/api/ajcaptcha",
            "/api/ajcheck",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/chat/**",  // 聊天界面静态资源
            "/customerServer.js"  // 客服聊天组件JS
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // 跳过OPTIONS请求(CORS预检请求)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 跳过不需要认证的URL
        if (isExcluded(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取Token (支持 Authori-zation 和 Authorization)
        String token = getToken(httpRequest);

        if (token == null || token.isEmpty()) {
            returnUnauthorized(httpResponse, "Authentication token is missing.");
            return;
        }
        if(httpRequest.getServletPath().startsWith("/api/mobile")){
           Map result= adminApplicationService.parseToken(token,null);
           if(result==null){
                returnUnauthorized(httpResponse, "Invalid token.");
                return;
            }
            Map appInfo = (Map) result.get("appInfo");
            CrmChatUser user = new CrmChatUser();
            String appid = (String) appInfo.get("appid");
            user.setAppid(appid);
            user.setToken(token);
            UserContext.setUser(user);
        }else {
            // 验证Token
            DecodedJWT jwt = jwtUtils.verifyToken(token);
            if (jwt == null) {
                returnUnauthorized(httpResponse, "Invalid or expired token.");
                return;
            }

            // 检查Token是否过期
            if (jwtUtils.isTokenExpired(token)) {
                returnUnauthorized(httpResponse, "Token has expired.");
                return;
            }

            // 设置用户上下文
            CrmChatUser user = new CrmChatUser();
            Long userId = jwt.getClaim("user_id").asLong();
            String username = jwt.getClaim("username").asString();
            String appid = jwt.getClaim("appid").asString();

            // 【安全增强】验证租户状态（方案1实现）
            // 如果不是管理员登录，需要验证租户账号是否存在、启用且未过期
            if (!"10000".equals(appid)) {
                TenantsEntity tenant = tenantsMapper.selectOne(
                    new QueryWrapper<TenantsEntity>().eq("appid", appid)
                );

                if (tenant == null) {
                    log.warn("[Security] Tenant is missing or has been deleted, appid={}, userId={}", appid, userId);
                    returnUnauthorized(httpResponse, "Tenant does not exist or has been removed. Please contact the administrator.");
                    return;
                }

                TenantStatus status = TenantStatus.fromCode(tenant.getStatus());
                if (status != TenantStatus.APPROVED) {
                    log.warn("[Security] Tenant status is invalid, appid={}, status={}, userId={}", appid, status, userId);
                    returnUnauthorized(httpResponse, "Tenant is disabled or in an invalid state. Please contact the administrator.");
                    return;
                }

                if (isTenantExpired(tenant.getExpireAt())) {
                    log.warn("[Security] Tenant subscription has expired, appid={}, expireAt={}, userId={}", appid, tenant.getExpireAt(), userId);
                    returnUnauthorized(httpResponse, "Tenant subscription has expired. Please contact the administrator to renew.");
                    return;
                }

                log.debug("[Security] Tenant status check passed, appid={}, userId={}", appid, userId);
            }

            user.setUserId(userId);
            user.setUsername(username);
            user.setAppid(appid);
            user.setToken(token);

            // 如果是管理员登录（appid = "10000"），加载角色和等级信息
            if ("10000".equals(appid)) {
                try {
                    SystemAdminEntity adminInfo = systemAdminMapper.selectById(userId.intValue());
                    if (adminInfo != null) {
                        user.setRoles(adminInfo.getRoles());
                        user.setLevel(adminInfo.getLevel());
                    }
                } catch (Exception e) {
                    log.warn("Failed to load administrator role information: userId={}", userId, e);
                }
            }

            UserContext.setUser(user);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束后清除用户上下文
            UserContext.clear();
        }
    }

    /**
     * 获取Token
     * 优先使用 Authori-zation，其次使用 Authorization
     */
    private String getToken(HttpServletRequest request) {
        // PHP使用的非标准请求头
        String token = request.getHeader(ApiConstants.AUTH_HEADER);

        // 如果没有，尝试标准请求头
        if (token == null || token.isEmpty()) {
            token = request.getHeader("Authorization");
        }

        if (token != null && token.startsWith(ApiConstants.TOKEN_PREFIX)) {
            return token.substring(ApiConstants.TOKEN_PREFIX.length());
        }

        return token;
    }

    /**
     * 判断URL是否需要排除认证
     */
    private boolean isExcluded(String uri) {
        for (String pattern : EXCLUDE_URL_PATTERNS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回未授权响应
     */
    private void returnUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResult<Object> result = ApiResult.fail(message);
        result.setStatus(401);

        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 判断租户是否过期
     */
    private boolean isTenantExpired(Timestamp expireAt) {
        if (expireAt == null) {
            return false; // null 表示永久有效
        }
        return System.currentTimeMillis() >= expireAt.getTime();
    }
}
