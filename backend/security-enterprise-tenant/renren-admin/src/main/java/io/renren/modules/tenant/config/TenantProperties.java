package io.renren.modules.tenant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "renren.tenant")
public class TenantProperties {

    /**
     * 是否开启多租户
     */
    private boolean enable = true;

    /**
     * 忽略的url
     */
    private Set<String> ignoreUrls;

}
