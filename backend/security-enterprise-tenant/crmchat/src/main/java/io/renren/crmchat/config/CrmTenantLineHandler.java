package io.renren.crmchat.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import io.renren.crmchat.security.TenantContextUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis Plus 租户处理器，自动为包含 appid 字段的表注入租户条件。
 */
@Slf4j
@Component
public class CrmTenantLineHandler implements TenantLineHandler, InitializingBean {

    private static final String ENTITY_BASE_PACKAGE = "io.renren.crmchat.entity";

    private final Set<String> tenantTables = ConcurrentHashMap.newKeySet();

    @Value("${crmchat.tenant.enable-auto-scan:true}")
    private boolean enableAutoScan;

    @Override
    public Expression getTenantId() {
        String appid = TenantContextUtils.currentAppid();
        if (appid == null) {
            return new NullValue();
        }
        return new StringValue(appid);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (!TenantContextUtils.isTenantIsolationEnabled()) {
            return true;
        }
        if (!enableAutoScan) {
            return false;
        }
        return !tenantTables.contains(tableName.toLowerCase(Locale.ROOT));
    }

    @Override
    public String getTenantIdColumn() {
        return "appid";
    }

    @Override
    public void afterPropertiesSet() {
        if (!enableAutoScan) {
            log.info("Tenant auto-scan disabled, tenant tables must be configured manually");
            return;
        }
        scanEntityTables();
        log.info("Registered {} tenant tables for isolation", tenantTables.size());
    }

    private void scanEntityTables() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TableName.class));

        scanner.findCandidateComponents(ENTITY_BASE_PACKAGE).forEach(beanDefinition -> {
            try {
                Class<?> clazz = ClassUtils.forName(beanDefinition.getBeanClassName(),
                        ClassUtils.getDefaultClassLoader());
                TableName tableName = clazz.getAnnotation(TableName.class);
                if (tableName == null || tableName.value().isEmpty()) {
                    return;
                }
                if (hasAppidColumn(clazz)) {
                    tenantTables.add(tableName.value().toLowerCase(Locale.ROOT));
                }
            } catch (ClassNotFoundException ex) {
                log.warn("Unable to resolve entity class: {}", beanDefinition.getBeanClassName(), ex);
            }
        });
    }

    private boolean hasAppidColumn(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return false;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if ("appid".equalsIgnoreCase(field.getName())) {
                return true;
            }
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && "appid".equalsIgnoreCase(tableField.value())) {
                return true;
            }
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null && "appid".equalsIgnoreCase(tableId.value())) {
                return true;
            }
        }
        return hasAppidColumn(clazz.getSuperclass());
    }
}
