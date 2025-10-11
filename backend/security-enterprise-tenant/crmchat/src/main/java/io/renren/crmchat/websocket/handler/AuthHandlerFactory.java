package io.renren.crmchat.websocket.handler;

import io.renren.common.utils.SpringContextUtils;
import io.renren.crmchat.websocket.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class AuthHandlerFactory {

    private static final Map<String, Class<? extends BaseHandler>> HANDLER_MAP = Map.of(
            "kefu", KefuHandler.class,
            "user", UserHandler.class,
            "admin", AdminHandler.class
    );

    /**
     * 根据用户类型获取对应的认证处理器
     *
     * @param userType 用户类型 (kefu, user, admin)
     * @return 对应的认证处理器，如果不支持则返回null
     */
    public BaseHandler getHandler(String userType) {
        if (userType == null || userType.isBlank()) {
            log.warn("User type is null or empty");
            return null;
        }

        String normalizedType = userType.toLowerCase(Locale.ROOT);
        Class<? extends BaseHandler> handlerClass = HANDLER_MAP.get(normalizedType);

        if (handlerClass == null) {
            log.warn("Unsupported user type: {}", userType);
            return null;
        }

        try {
            return SpringContextUtils.getBean(handlerClass);
        } catch (Exception e) {
            log.error("Failed to get handler for user type: {}", userType, e);
            return null;
        }
    }

    /**
     * 检查是否支持指定的用户类型
     *
     * @param userType 用户类型
     * @return 如果支持返回true，否则返回false
     */
    public boolean isSupported(String userType) {
        if (userType == null || userType.isBlank()) {
            return false;
        }
        return HANDLER_MAP.containsKey(userType.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取所有支持的用户类型
     *
     * @return 支持的用户类型集合
     */
    public java.util.Set<String> getSupportedUserTypes() {
        return HANDLER_MAP.keySet();
    }
}