package io.renren.crmchat.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.renren.crmchat.interceptor.AdminLogInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimeZone;

/**
 * Web MVC 配置
 *
 * @author CRMChat Team
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 文件上传根目录
     */
    @Value("${file.upload.path:./uploads}")
    private String uploadBasePath;

    private final AdminLogInterceptor adminLogInterceptor;

    public WebMvcConfig(AdminLogInterceptor adminLogInterceptor) {
        this.adminLogInterceptor = adminLogInterceptor;
    }

    /**
     * 配置静态资源映射
     * 将HTTP请求路径映射到文件系统的uploads目录
     *
     * PHP参考：public目录下直接访问uploads文件夹
     * HTTP路径格式：/tenant/attach/2025-10-13/xxx.png
     * 映射到物理路径：uploads/tenant/attach/2025-10-13/xxx.png
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径
        String absoluteUploadPath = Paths.get(uploadBasePath).toAbsolutePath().normalize().toString();

        // 确保路径以文件URL格式结尾
        String fileUrl = "file:///" + absoluteUploadPath.replace("\\", "/");
        if (!fileUrl.endsWith("/")) {
            fileUrl += "/";
        }

        log.info("Configuring static resource mapping:");
        log.info("  Upload base path: {}", absoluteUploadPath);
        log.info("  File URL: {}", fileUrl);

        // 映射租户上传文件（tenant、admin、merchant等appId）
        // 例如：http://localhost:20108/tenant/attach/2025-10-13/xxx.png
        // 映射到：file:///path/to/uploads/tenant/attach/2025-10-13/xxx.png
        registry.addResourceHandler("/tenant/**")
                .addResourceLocations(fileUrl + "tenant/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/admin/**")
                .addResourceLocations(fileUrl + "admin/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/merchant/**")
                .addResourceLocations(fileUrl + "merchant/")
                .setCachePeriod(3600);

        log.info("Static resource handlers configured for: /tenant/**, /admin/**, /merchant/**");
    }

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
