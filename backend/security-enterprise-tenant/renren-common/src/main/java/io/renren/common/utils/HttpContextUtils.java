/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

import io.renren.common.constant.Constant;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Http
 *
 * @author Mark sunlightcs@gmail.com
 */
public class HttpContextUtils {

    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Enumeration<String> parameters = request.getParameterNames();

        Map<String, String> params = new HashMap<>();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            String value = request.getParameter(parameter);
            if (StringUtils.isNotBlank(value)) {
                params.put(parameter, value);
            }
        }

        return params;
    }

    public static String getVisitDomain() {
        HttpServletRequest request = getHttpServletRequest();
        String domain = request.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.isBlank(domain)) {
            domain = request.getHeader(HttpHeaders.REFERER);
        }
        domain = StringUtils.removeEnd(domain, "/");
        return domain;
    }

    public static String getOrigin() {
        HttpServletRequest request = getHttpServletRequest();
        return request.getHeader(HttpHeaders.ORIGIN);
    }

    public static String getTenantCode() {
        HttpServletRequest request = getHttpServletRequest();
        String tenantCode = request.getHeader(Constant.TENANT_CODE);
        if (StringUtils.isBlank(tenantCode)) {
            tenantCode = request.getParameter(Constant.TENANT_CODE);
        }
        return tenantCode;
    }

    public static String getLanguage() {
        //默认语言
        String defaultLanguage = "zh-CN";
        //request
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return defaultLanguage;
        }

        //请求语言
        defaultLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);

        return defaultLanguage;
    }
}