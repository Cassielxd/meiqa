package io.renren.modules.tenant.config;

import io.renren.modules.tenant.service.SysTenantService;
import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;

@AllArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "renren.tenant", value = "enable", havingValue = "true")
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfig {
    private final SysTenantService sysTenantService;
    private final TenantProperties tenantProperties;
    private final PathMatcher pathMatcher;

    @Bean
    public InitTenantDataSource tenantInterceptor() {
        return new InitTenantDataSource();
    }

    @Bean
    public FilterRegistrationBean<TenantRequestFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantRequestFilter(sysTenantService, tenantProperties, pathMatcher));
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.addUrlPatterns("/*");
        registration.setName("tenantFilter");
        registration.setOrder(0);
        return registration;
    }

}
