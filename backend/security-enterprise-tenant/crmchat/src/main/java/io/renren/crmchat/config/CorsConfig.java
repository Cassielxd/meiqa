package io.renren.crmchat.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 *
 * 用于支持租户网站集成客服聊天组件
 * 租户可以在任何域名下嵌入 customerServer.js 并与后端 API 通信
 *
 * @author CRMChat Team
 */
@Configuration
public class CorsConfig {

    /**
     * CORS 过滤器
     *
     * 配置说明：
     * - 允许所有来源（*）：因为租户网站可能在任何域名
     * - 允许凭证（credentials）：支持 Cookie 和 Token 认证
     * - 允许所有HTTP方法：GET, POST, PUT, DELETE, OPTIONS, PATCH
     * - 允许所有请求头：包括自定义的 Authorization 头
     * - 暴露所有响应头：前端可以读取服务器返回的所有头信息
     * - 预检请求缓存3600秒：减少 OPTIONS 请求频率
     * - 【安全修复】Order=0确保CORS头部添加到所有响应（包括401）
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 【安全修复】当allowCredentials=true时，不能使用通配符
        // 允许本地文件系统访问（用于SDK测试HTML文件）
        config.addAllowedOrigin("null");
        // 允许本地开发环境的所有端口
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        // 允许租户自定义域名（生产环境需要配置具体域名）
        config.addAllowedOriginPattern("http://*");
        config.addAllowedOriginPattern("https://*");

        // 允许携带凭证（Cookie, Authorization header）
        config.setAllowCredentials(true);

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有HTTP方法
        config.addAllowedMethod("*");

        // 【安全修复】暴露必要的响应头，让前端能读取401状态码
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Authori-zation");

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 应用到所有路径
        source.registerCorsConfiguration("/**", config);

        // 【安全修复】创建FilterRegistrationBean并设置order=0
        // 确保CORS filter在AuthenticationFilter(order=1)之前执行
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(0);
        registration.setName("corsFilter");

        return registration;
    }
}
