package io.renren.crmchat.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类 - PHP兼容
 * 使用 firebase/php-jwt 兼容的算法
 *
 * @author CRMChat Team
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${crmchat.jwt.secret:your-secret-key-change-in-production}")
    private String secret;

    @Value("${crmchat.jwt.expire:7200}")
    private long expire;

    @Value("${kefu_path:''}")
    private String kefuPath;

    public String getKefuPath() {
        return kefuPath;
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param appid 租户appid
     * @return token
     */
    public String generateToken(Long userId, String username, String appid) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 1000);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withClaim("user_id", userId)
                .withClaim("username", username)
                .withClaim("appid", appid)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    /**
     * 验证Token并解析
     *
     * @param token JWT token
     * @return DecodedJWT
     */
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token（允许过期，用于刷新）
     */
    public DecodedJWT verifyTokenAllowExpired(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (TokenExpiredException expiredException) {
            try {
                DecodedJWT decodedJWT = JWT.decode(token);
                Algorithm algorithm = Algorithm.HMAC256(secret);
                algorithm.verify(decodedJWT);
                return decodedJWT;
            } catch (Exception inner) {
                log.error("JWT验证失败(过期): {}", inner.getMessage());
                return null;
            }
        } catch (JWTVerificationException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt != null ? jwt.getClaim("user_id").asLong() : null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsername(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt != null ? jwt.getClaim("username").asString() : null;
    }

    /**
     * 从Token中获取appid
     *
     * @param token JWT token
     * @return appid
     */
    public String getAppid(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt != null ? jwt.getClaim("appid").asString() : null;
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT token
     * @return true=过期
     */
    public boolean isTokenExpired(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt == null) {
            return true;
        }
        return jwt.getExpiresAt().before(new Date());
    }

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT token
     * @return 过期时间，可能为 null
     */
    public Date getExpiration(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt != null ? jwt.getExpiresAt() : null;
    }
}
