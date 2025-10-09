package io.renren.modules.tenant.config;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.renren.common.context.TenantContext;
import io.renren.common.enums.TenantModeEnum;
import io.renren.common.utils.HttpContextUtils;
import io.renren.common.utils.JsonUtils;
import io.renren.common.utils.Result;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.tenant.dto.SysTenantListDTO;
import io.renren.modules.tenant.service.SysTenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

/**
 * 租户 数据源切换
 *
 * @author Mark sunlightcs@gmail.com
 */
public class TenantRequestFilter extends OncePerRequestFilter {
    private final SysTenantService sysTenantService;
    private final TenantProperties tenantProperties;
    private final PathMatcher pathMatcher;

    public TenantRequestFilter(SysTenantService sysTenantService, TenantProperties tenantProperties,
                               PathMatcher pathMatcher) {
        this.sysTenantService = sysTenantService;
        this.tenantProperties = tenantProperties;
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isIgnoreUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String domain = HttpContextUtils.getVisitDomain();
        final Long tenantCode = TenantContext.getTenantCode(SecurityUser.getUser());

        List<SysTenantListDTO> tenantList = sysTenantService.list();
        SysTenantListDTO sysTenant = tenantList.stream().filter(tenant -> Objects.equals(tenant.getTenantCode(), tenantCode)).findFirst().orElse(null);

        for (SysTenantListDTO tenant : tenantList) {
            if (StrUtil.equalsIgnoreCase(tenant.getTenantDomain(), domain)) {
                sysTenant = tenant;
                break;
            }
        }

        // 租户不存在
        if (sysTenant == null) {
            Result<Object> result = new Result<>().error("Tenant code: " + tenantCode + " does not exist, please contact administrator");
            writeJSON(request, response, result);
            return;
        }

        // 当前访问者租户编码
        TenantContext.setVisitorTenantCode(sysTenant.getTenantCode());

        // 切换数据源
        if (ObjectUtil.equals(sysTenant.getTenantMode(), TenantModeEnum.DATASOURCE.value())) {
            DynamicContextHolder.push(sysTenant.getDatasourceId() + "");
        }

        filterChain.doFilter(request, response);

        // 清除数据源
        if (ObjectUtil.equals(sysTenant.getTenantMode(), TenantModeEnum.DATASOURCE.value())) {
            DynamicContextHolder.poll();
        }
    }

    private boolean isIgnoreUrl(HttpServletRequest request) {
        for (String url : tenantProperties.getIgnoreUrls()) {
            if (pathMatcher.match(url, request.getServletPath())) {
                return true;
            }
        }
        return false;
    }

    private void writeJSON(HttpServletRequest request, HttpServletResponse response, Object object) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader(HttpHeaders.ORIGIN));

        String content = JsonUtils.toJsonString(object);
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new UtilException(e);
        } finally {
            IoUtil.close(writer);
        }
    }
}
