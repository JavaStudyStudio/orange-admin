package com.orange.demo.common.core.cache;

import com.orange.demo.common.core.object.TokenData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Session数据缓存辅助类。
 *
 * @author Jerry
 * @date 2020-09-24
 */
@SuppressWarnings("unchecked")
@Component
public class SessionCacheHelper {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 缓存当前session内，上传过的文件名。
     *
     * @param filename 通常是本地存储的文件名，而不是上传时的原始文件名。
     */
    public void putSessionUploadFile(String filename) {
        if (filename != null) {
            Set<String> sessionUploadFileSet = null;
            Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.UPLOAD_FILENAME_CACHE.name());
            Cache.ValueWrapper valueWrapper = cache.get(TokenData.takeFromRequest().getSessionId());
            if (valueWrapper != null) {
                sessionUploadFileSet = (Set<String>) valueWrapper.get();
            }
            if (sessionUploadFileSet == null) {
                sessionUploadFileSet = new HashSet<>();
            }
            sessionUploadFileSet.add(filename);
            cache.put(TokenData.takeFromRequest().getSessionId(), sessionUploadFileSet);
        }
    }

    /**
     * 判断参数中的文件名，是否有当前session上传。
     *
     * @param filename 通常是本地存储的文件名，而不是上传时的原始文件名。
     * @return true表示该文件是由当前session上传并存储在本地的，否则false。
     */
    public boolean existSessionUploadFile(String filename) {
        if (filename == null) {
            return false;
        }
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.UPLOAD_FILENAME_CACHE.name());
        Cache.ValueWrapper valueWrapper = cache.get(TokenData.takeFromRequest().getSessionId());
        if (valueWrapper == null) {
            return false;
        }
        return ((Set<String>) valueWrapper.get()).contains(filename);
    }

    /**
     * 存放session的Token数据。
     *
     * @param sessionId 当前会话的SessionId。
     * @param tokenData 当前会话的JWT Token对象。
     */
    public void putTokenData(String sessionId, TokenData tokenData) {
        if (sessionId == null || tokenData == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.GLOBAL_CACHE.name());
        cache.put(sessionId, tokenData);
    }

    /**
     * 获取session的JWT Token对象。
     *
     * @param sessionId 当前会话的SessionId。
     * @return 当前会话的JWT Token对象。
     */
    public TokenData getTokenData(String sessionId) {
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.GLOBAL_CACHE.name());
        return cache.get(sessionId, TokenData.class);
    }

    /**
     * 清除当前session的所有缓存数据。
     */
    public void removeAllSessionCache() {
        for (CacheConfig.CacheEnum c : CacheConfig.CacheEnum.values()) {
            cacheManager.getCache(c.name()).clear();
        }
    }
}
