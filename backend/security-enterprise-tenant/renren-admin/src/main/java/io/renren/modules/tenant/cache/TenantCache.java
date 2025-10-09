package io.renren.modules.tenant.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.renren.common.redis.RedisKeys;
import io.renren.common.redis.RedisUtils;
import io.renren.modules.tenant.dto.SysTenantListDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 租户缓存
 */
@Component
public class TenantCache {
    @Resource
    private RedisUtils redisUtils;
    @Value("${renren.redis.open}")
    private boolean open;

    /**
     * Local Cache  30天过期
     */
    private final Cache<String, List<SysTenantListDTO>> localCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.DAYS).build();

    public void setCache(List<SysTenantListDTO> list) {
        String key = RedisKeys.getTenantKey();
        if (open) {
            redisUtils.set(key, list);
        } else {
            localCache.put(key, list);
        }
    }

    public List<SysTenantListDTO> getCache() {
        List<SysTenantListDTO> list;
        String key = RedisKeys.getTenantKey();
        if (open) {
            list = (List<SysTenantListDTO>) redisUtils.get(key);
        } else {
            list = localCache.getIfPresent(key);
        }

        return list;
    }

    /**
     * 清空缓存
     */
    public void clear() {
        String key = RedisKeys.getTenantKey();
        if (open) {
            redisUtils.delete(key);
        } else {
            localCache.invalidateAll();
        }
    }
}
