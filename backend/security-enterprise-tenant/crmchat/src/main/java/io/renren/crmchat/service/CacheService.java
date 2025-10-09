package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.CacheMapper;
import io.renren.crmchat.entity.CacheEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 缓存服务
 * PHP Reference: app/services/other/CacheServices.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class CacheService {

    private final CacheMapper cacheMapper;

    /**
     * 获取缓存数据
     * PHP Reference: CacheServices::getDbCache()
     *
     * @param key 缓存键
     * @param defaultValue 默认值
     * @return 缓存值或默认值
     */
    public String getDbCache(String key, String defaultValue) {
        // 删除过期缓存
        deleteOverdueCache();

        CacheEntity cache = cacheMapper.selectOne(
            new QueryWrapper<CacheEntity>().eq("`key`", key)
        );

        if (cache != null && cache.getResult() != null) {
            // PHP存储时使用了json_encode，但对于字符串会加双引号
            // 需要去掉JSON字符串的引号
            String result = cache.getResult();
            if (result.startsWith("\"") && result.endsWith("\"")) {
                return result.substring(1, result.length() - 1);
            }
            return result;
        }

        // 如果不存在，设置默认值并返回
        if (defaultValue != null) {
            setDbCache(key, defaultValue, 0);
        }
        return defaultValue;
    }

    /**
     * 设置缓存数据
     * PHP Reference: CacheServices::setDbCache()
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param expire 过期时间（秒），0表示永不过期
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean setDbCache(String key, String value, int expire) {
        // 删除过期缓存
        deleteOverdueCache();

        int currentTime = (int) (System.currentTimeMillis() / 1000);
        int expireTime = expire > 0 ? currentTime + expire : 0;

        // PHP: json_encode($result)
        // 对于简单字符串，PHP的json_encode会加双引号
        String jsonValue = "\"" + value + "\"";

        CacheEntity existing = cacheMapper.selectOne(
            new QueryWrapper<CacheEntity>().eq("`key`", key)
        );

        if (existing != null) {
            // 更新现有记录
            existing.setResult(jsonValue);
            existing.setExpireTime(expireTime);
            existing.setAddTime(currentTime);
            return cacheMapper.updateById(existing) > 0;
        } else {
            // 插入新记录
            CacheEntity cache = new CacheEntity();
            cache.setKey(key);
            cache.setResult(jsonValue);
            cache.setExpireTime(expireTime);
            cache.setAddTime(currentTime);
            return cacheMapper.insert(cache) > 0;
        }
    }

    /**
     * 删除过期缓存
     * PHP Reference: CacheServices::delectDeOverdueDbCache()
     */
    private void deleteOverdueCache() {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        cacheMapper.delete(
            new QueryWrapper<CacheEntity>()
                .gt("expire_time", 0)
                .lt("expire_time", currentTime)
        );
    }
}
