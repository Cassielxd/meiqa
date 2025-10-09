package io.renren.crmchat.service.common;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.renren.crmchat.security.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Token 服务 - 统一 JWT Token 管理
 *
 * 职责：
 * 1. 封装 JwtUtils，提供更高级的业务 API
 * 2. 统一 Token 生成逻辑
 * 3. Token 验证和解析
 * 4. 用户身份提取
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;

    /**
     * 生成用户 Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param appid 应用ID（租户ID）
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String appid) {
        return jwtUtils.generateToken(userId, username, appid);
    }

    /**
     * 生成管理员 Token（固定 appid = "10000"）
     *
     * @param adminId 管理员ID
     * @param adminAccount 管理员账号
     * @return JWT Token
     */
    public String generateAdminToken(Long adminId, String adminAccount) {
        return jwtUtils.generateToken(adminId, adminAccount, "10000");
    }

    /**
     * 生成租户 Token（使用租户的 appid）
     *
     * @param tenantId 租户ID
     * @param tenantAccount 租户账号
     * @param tenantAppid 租户的 appid
     * @return JWT Token
     */
    public String generateTenantToken(Long tenantId, String tenantAccount, String tenantAppid) {
        return jwtUtils.generateToken(tenantId, tenantAccount, tenantAppid);
    }

    /**
     * 生成客服 Token（使用所属租户的 appid）
     *
     * @param kefuId 客服ID
     * @param kefuAccount 客服账号
     * @param tenantAppid 所属租户的 appid
     * @return JWT Token
     */
    public String generateKefuToken(Long kefuId, String kefuAccount, String tenantAppid) {
        return jwtUtils.generateToken(kefuId, kefuAccount, tenantAppid);
    }

    /**
     * 验证 Token 有效性
     *
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        DecodedJWT jwt = jwtUtils.verifyToken(token);
        return jwt != null && !jwtUtils.isTokenExpired(token);
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，失败返回 null
     */
    public Long extractUserId(String token) {
        return jwtUtils.getUserId(token);
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token
     * @return 用户名，失败返回 null
     */
    public String extractUsername(String token) {
        return jwtUtils.getUsername(token);
    }

    /**
     * 从 Token 中提取 appid
     *
     * @param token JWT Token
     * @return appid，失败返回 null
     */
    public String extractAppid(String token) {
        return jwtUtils.getAppid(token);
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return true=已过期
     */
    public boolean isTokenExpired(String token) {
        return jwtUtils.isTokenExpired(token);
    }

    /**
     * 获取 Token 的过期时间戳（秒）
     *
     * @param token JWT Token
     * @return 过期时间戳（秒），若解析失败返回 null
     */
    public Long getTokenExpireAt(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        java.util.Date expiration = jwtUtils.getExpiration(token);
        if (expiration == null) {
            return null;
        }
        return expiration.getTime() / 1000;
    }

    /**
     * 刷新 Token（重新生成）
     *
     * @param oldToken 旧的 Token
     * @return 新的 Token，失败返回 null
     */
    public String refreshToken(String oldToken) {
        if (!validateToken(oldToken)) {
            return null;
        }

        Long userId = extractUserId(oldToken);
        String username = extractUsername(oldToken);
        String appid = extractAppid(oldToken);

        if (userId == null || username == null || appid == null) {
            return null;
        }

        return generateToken(userId, username, appid);
    }

    /**
     * 验证 Token 的 appid 是否匹配
     *
     * 使用场景：验证用户是否属于指定租户
     *
     * @param token JWT Token
     * @param expectedAppid 期望的 appid
     * @return true=匹配
     */
    public boolean verifyAppid(String token, String expectedAppid) {
        if (!validateToken(token)) {
            return false;
        }

        String actualAppid = extractAppid(token);
        return expectedAppid != null && expectedAppid.equals(actualAppid);
    }

    /**
     * 验证是否为管理员 Token
     *
     * @param token JWT Token
     * @return true=管理员 Token
     */
    public boolean isAdminToken(String token) {
        return verifyAppid(token, "10000");
    }
}
