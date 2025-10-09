package io.renren.crmchat.service;

import io.renren.common.redis.RedisUtils;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 简易版扫码登录凭证管理器，用于兼容 PHP 的 CacheService 行为。
 *
 * <p>状态定义与 PHP 对齐：</p>
 * <ul>
 *   <li>0 - 凭证不存在或已过期，需要刷新二维码</li>
 *   <li>1 - 已扫描等待确认（PHP 中缓存值为 '0'）</li>
 *   <li>2 - 尚未扫描（PHP 中缓存值为 '1'）</li>
 *   <li>3 - 扫描成功并完成登录</li>
 * </ul>
 */
@Component
public class KefuLoginCodeManager {

    private static final int EXPIRE_SECONDS = 600;
    private static final String REDIS_KEY_PREFIX = "crmchat:kefu:login:";

    private final RedisUtils redisUtils;

    public KefuLoginCodeManager(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public Map<String, Object> generateLoginKey() {
        String key = UUID.randomUUID().toString().replace("-", "");
        long expireAt = Instant.now().plusSeconds(EXPIRE_SECONDS).getEpochSecond();
        LoginCode code = new LoginCode(expireAt, 2, null);
        redisUtils.set(redisKey(key), code, EXPIRE_SECONDS);
        return Map.of("key", key, "time", expireAt);
    }

    public LoginCode get(String key) {
        if (key == null) {
            return null;
        }
        Object cached = redisUtils.get(redisKey(key));
        if (cached instanceof LoginCode code) {
            if (code.isExpired()) {
                redisUtils.delete(redisKey(key));
                return null;
            }
            return code;
        }
        return null;
    }

    public void markScanned(String key, Integer kefuId) {
        if (key == null) {
            return;
        }
        LoginCode code = get(key);
        if (code != null) {
            code.setStatus(1);
            code.setKefuId(kefuId);
            redisUtils.set(redisKey(key), code, EXPIRE_SECONDS);
        }
    }

    public void consume(String key) {
        if (key != null) {
            redisUtils.delete(redisKey(key));
        }
    }

    private String redisKey(String key) {
        return REDIS_KEY_PREFIX + key;
    }

    public static class LoginCode implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final long expireAt;
        private volatile int status;
        private volatile Integer kefuId;

        public LoginCode(long expireAt, int status, Integer kefuId) {
            this.expireAt = expireAt;
            this.status = status;
            this.kefuId = kefuId;
        }

        public long getExpireAt() {
            return expireAt;
        }

        public boolean isExpired() {
            return Instant.now().getEpochSecond() > expireAt;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Integer getKefuId() {
            return kefuId;
        }

        public void setKefuId(Integer kefuId) {
            this.kefuId = kefuId;
        }
    }
}
