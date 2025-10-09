package io.renren.crmchat.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.renren.crmchat.interceptor.AdminLogInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.TimeZone;

/**
 * Web MVC 配置
 *
 * @author CRMChat Team
 */
@Configuration
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminLogInterceptor adminLogInterceptor;

    /**
     * 添加拦截器
     * PHP Reference: AdminLogMiddleware 中间件注册
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册管理员日志拦截器
        // PHP: Route::middleware([AdminLogMiddleware::class])
        registry.addInterceptor(adminLogInterceptor)
                .addPathPatterns("/api/admin/**")  // 拦截所有管理员API
                .excludePathPatterns("/api/admin/login/**");  // 排除登录接口
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS跨域支持 - 允许所有租户网站集成客服组件
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源（租户网站可能在任何域名）
                .allowCredentials(true)       // 允许携带凭证
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")          // 允许所有请求头
                .exposedHeaders("*")          // 暴露所有响应头
                .maxAge(3600);                // 预检请求缓存1小时
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jackson2HttpMessageConverter());
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();

        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 设置时区为东八区
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        // Long类型转String类型，避免前端精度丢失
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        mapper.registerModule(simpleModule);

        converter.setObjectMapper(mapper);
        return converter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return jackson2HttpMessageConverter().getObjectMapper();
    }
}
