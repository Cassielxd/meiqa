package io.renren.crmchat.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.dao.SystemAdminMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthenticationFilter 安全测试
 * 验证P0级安全修复: 白名单精确匹配机制
 *
 * @author CRMChat Team
 */
@DisplayName("认证过滤器安全测试")
class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private DecodedJWT decodedJWT;

    @Mock
    private SystemAdminMapper systemAdminMapper;

    @Mock
    private io.renren.crmchat.dao.TenantsMapper tenantsMapper;

    @Mock
    private io.renren.crmchat.service.AdminApplicationService adminApplicationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        authenticationFilter = new AuthenticationFilter(jwtUtils, objectMapper, systemAdminMapper, tenantsMapper, adminApplicationService);
    }

    // ==================== P0安全修复验证: 白名单精确匹配 ====================

    @Test
    @DisplayName("TC-SEC-001.1: 白名单路径应该放行 - auto_login精确匹配")
    void testWhitelistPathExactMatch_AutoLogin() throws IOException, ServletException {
        // 精确匹配白名单路径: /api/mobile/service/auto_login
        when(request.getRequestURI()).thenReturn("/api/mobile/service/auto_login");
        when(request.getMethod()).thenReturn("POST");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 应该放行,不返回401
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("TC-SEC-001.2: 白名单路径不应前缀匹配 - auto_login/extra应拦截")
    void testWhitelistPathNoPrefix_AutoLoginExtra() throws IOException, ServletException {
        // 非精确匹配路径: /api/mobile/service/auto_login/extra
        // 旧版本bug: contains()会误放行此路径
        // 新版本fix: AntPathMatcher精确匹配,应拦截
        when(request.getRequestURI()).thenReturn("/api/mobile/service/auto_login/extra");
        when(request.getMethod()).thenReturn("GET");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 应该被拦截(无Token)
        verify(filterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("TC-SEC-001.3: 非白名单路径应拦截 - /api/mobile/service/send_message")
    void testNonWhitelistPath_ShouldReject() throws IOException, ServletException {
        // 非白名单路径,需要认证
        when(request.getRequestURI()).thenReturn("/api/mobile/service/send_message");
        when(request.getMethod()).thenReturn("POST");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 应该被拦截
        verify(filterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("TC-SEC-001.4: 多个白名单路径验证")
    void testMultipleWhitelistPaths() throws IOException, ServletException {
        String[] whitelistPaths = {
                "/api/admin/login",
                "/api/tenant/login",
                "/api/kefu/login",
                "/api/mobile/login",
                "/api/mobile/service/auto_login",
                "/swagger-ui.html"
        };

        for (String path : whitelistPaths) {
            // 重置mock
            reset(request, response, filterChain);
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("POST");

            authenticationFilter.doFilter(request, response, filterChain);

            // 验证: 所有白名单路径都应该放行
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("TC-SEC-001.5: OPTIONS请求应放行(CORS预检)")
    void testOptionsRequest_ShouldPass() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("OPTIONS");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: OPTIONS请求应该放行
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== Token验证链路测试 ====================

    @Test
    @DisplayName("TC-SEC-002.1: 无Token请求应返回401")
    void testNoToken_ShouldReturn401() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);

        // 验证响应消息
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("未提供认证Token"));
    }

    @Test
    @DisplayName("TC-SEC-002.2: 无效Token应返回401")
    void testInvalidToken_ShouldReturn401() throws IOException, ServletException {
        String invalidToken = "invalid_token_string";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.verifyToken(invalidToken)).thenReturn(null); // Token验证失败

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(jwtUtils, times(1)).verifyToken(invalidToken);
        verify(filterChain, never()).doFilter(request, response);

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("Token无效或已过期"));
    }

    @Test
    @DisplayName("TC-SEC-002.3: 过期Token应返回401")
    void testExpiredToken_ShouldReturn401() throws IOException, ServletException {
        String expiredToken = "expired_token";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn("Bearer " + expiredToken);
        when(jwtUtils.verifyToken(expiredToken)).thenReturn(decodedJWT); // Token有效
        when(jwtUtils.isTokenExpired(expiredToken)).thenReturn(true); // 但已过期

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(jwtUtils, times(1)).isTokenExpired(expiredToken);
        verify(filterChain, never()).doFilter(request, response);

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("Token已过期"));
    }

    @Test
    @DisplayName("TC-SEC-002.4: 有效Token应设置UserContext并放行")
    void testValidToken_ShouldSetUserContextAndPass() throws IOException, ServletException {
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn("Bearer " + validToken);

        // Mock JWT claims
        when(jwtUtils.verifyToken(validToken)).thenReturn(decodedJWT);
        when(jwtUtils.isTokenExpired(validToken)).thenReturn(false);
        when(decodedJWT.getClaim("user_id")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("user_id").asLong()).thenReturn(123L);
        when(decodedJWT.getClaim("username")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("username").asString()).thenReturn("test_user");
        when(decodedJWT.getClaim("appid")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("appid").asString()).thenReturn("tenant_A");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证
        verify(jwtUtils, times(1)).verifyToken(validToken);
        verify(jwtUtils, times(1)).isTokenExpired(validToken);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 注意: UserContext.clear() 会在finally中调用,这里无法直接验证
        // 但可以通过集成测试验证UserContext的生命周期
    }

    @Test
    @DisplayName("TC-SEC-002.5: 支持PHP兼容的Authori-zation请求头")
    void testPhpCompatibleHeader_AuthoriZation() throws IOException, ServletException {
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        // PHP使用的非标准请求头: Authori-zation (中间有破折号)
        when(request.getHeader("Authori-zation")).thenReturn("Bearer " + validToken);
        when(request.getHeader("Authorization")).thenReturn(null);

        when(jwtUtils.verifyToken(validToken)).thenReturn(decodedJWT);
        when(jwtUtils.isTokenExpired(validToken)).thenReturn(false);
        when(decodedJWT.getClaim("user_id")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("user_id").asLong()).thenReturn(123L);
        when(decodedJWT.getClaim("username")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("username").asString()).thenReturn("test_user");
        when(decodedJWT.getClaim("appid")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("appid").asString()).thenReturn("tenant_A");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: PHP请求头应该被识别
        verify(jwtUtils, times(1)).verifyToken(validToken);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("TC-SEC-002.6: 标准Authorization请求头回退支持")
    void testStandardHeader_Authorization() throws IOException, ServletException {
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        // 如果没有Authori-zation,回退到标准的Authorization
        when(request.getHeader("Authori-zation")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        when(jwtUtils.verifyToken(validToken)).thenReturn(decodedJWT);
        when(jwtUtils.isTokenExpired(validToken)).thenReturn(false);
        when(decodedJWT.getClaim("user_id")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("user_id").asLong()).thenReturn(123L);
        when(decodedJWT.getClaim("username")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("username").asString()).thenReturn("test_user");
        when(decodedJWT.getClaim("appid")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("appid").asString()).thenReturn("tenant_A");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 标准请求头应该被识别
        verify(jwtUtils, times(1)).verifyToken(validToken);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("TC-SEC-003.1: 空Token字符串应拒绝")
    void testEmptyToken_ShouldReject() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn("Bearer ");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("TC-SEC-003.2: 无Bearer前缀的Token应正常处理")
    void testTokenWithoutBearerPrefix() throws IOException, ServletException {
        String validToken = "valid_token_no_prefix";
        when(request.getRequestURI()).thenReturn("/api/mobile/user/record");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authori-zation")).thenReturn(validToken); // 无Bearer前缀

        when(jwtUtils.verifyToken(validToken)).thenReturn(decodedJWT);
        when(jwtUtils.isTokenExpired(validToken)).thenReturn(false);
        when(decodedJWT.getClaim("user_id")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("user_id").asLong()).thenReturn(123L);
        when(decodedJWT.getClaim("username")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("username").asString()).thenReturn("test_user");
        when(decodedJWT.getClaim("appid")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("appid").asString()).thenReturn("tenant_A");

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 应该正常处理(兼容性考虑)
        verify(jwtUtils, times(1)).verifyToken(validToken);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("TC-SEC-003.3: 路径大小写敏感验证")
    void testPathCaseSensitivity() throws IOException, ServletException {
        // 测试路径大小写敏感(应该不匹配白名单)
        when(request.getRequestURI()).thenReturn("/API/MOBILE/SERVICE/AUTO_LOGIN");
        when(request.getMethod()).thenReturn("POST");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        authenticationFilter.doFilter(request, response, filterChain);

        // 验证: 大写路径不匹配白名单,应被拦截
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}
