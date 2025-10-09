package io.renren.crmchat.common.constant;

/**
 * API常量
 *
 * @author CRMChat
 */
public interface ApiConstants {

    /**
     * PHP兼容的认证头: Authori-zation (注意中间有连字符)
     */
    String AUTH_HEADER = "Authori-zation";

    /**
     * Token前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 默认appid (平台管理)
     */
    String DEFAULT_APPID = "10000";

    /**
     * 分页默认值
     */
    int DEFAULT_PAGE = 1;
    int DEFAULT_LIMIT = 10;
    int MAX_LIMIT = 100;
}
