package io.renren.crmchat.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.renren.crmchat.security.CrmChatUser;
import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.security.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis Plus 字段自动填充处理器
 * 适配JWT用户上下文
 *
 * @author CRMChat Team
 */
@Component
public class FieldMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_DATE = "createDate";
    private static final String CREATOR = "creator";
    private static final String UPDATE_DATE = "updateDate";
    private static final String UPDATER = "updater";
    private static final String APPID = "appid";

    @Override
    public void insertFill(MetaObject metaObject) {
        CrmChatUser user = UserContext.getUser();
        Date now = new Date();

        // 创建者
        if (user != null && user.getUserId() != null) {
            strictInsertFill(metaObject, CREATOR, Long.class, user.getUserId());
        }

        // 创建时间
        setFieldValByName(CREATE_DATE, now, metaObject);

        // 租户appid：仅在开启租户隔离时自动注入
        if (TenantContextUtils.isTenantIsolationEnabled()) {
            String appid = TenantContextUtils.currentAppid();
            if (appid != null && !appid.isEmpty()) {
                strictInsertFill(metaObject, APPID, String.class, appid);
            }
        }

        // 更新者
        if (user != null && user.getUserId() != null) {
            strictInsertFill(metaObject, UPDATER, Long.class, user.getUserId());
        }

        // 更新时间
        setFieldValByName(UPDATE_DATE, now, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        CrmChatUser user = UserContext.getUser();

        // 更新者
        if (user != null && user.getUserId() != null) {
            strictUpdateFill(metaObject, UPDATER, Long.class, user.getUserId());
        }

        // 更新时间
        strictUpdateFill(metaObject, UPDATE_DATE, Date.class, new Date());
    }
}
