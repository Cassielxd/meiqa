package io.renren.crmchat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 地理位置服务
 * 用于获取IP地址的地理位置信息
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
public class GeoLocationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 是否启用本地IP模拟数据
     * 开发环境建议开启，生产环境建议关闭
     */
    @Value("${geo.mock-local-ip:true}")
    private boolean mockLocalIp;

    /**
     * 获取IP地理位置信息
     * 优先使用 ip-api.com，失败后使用 seeip.org
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    public Map<String, Object> getGeoLocation(String ip) {
        Map<String, Object> result = new HashMap<>();
        
        // ✅ 如果IP为空或本地IP
        if (ip == null || ip.isEmpty() || isLocalIp(ip)) {
            if (mockLocalIp) {
                // 开发环境：返回模拟数据
                log.warn("⚠️ Local IP detected, returning mock geo data: {}", ip);
                return getMockGeoData(ip);
            } else {
                // 生产环境：返回失败
                log.warn("⚠️ Invalid or local IP address: {}", ip);
                result.put("success", false);
                result.put("message", "Invalid or local IP");
                return result;
            }
        }

        log.info("🌍 [GEO] Fetching geo location for IP: {}", ip);

        // 尝试主API：ip-api.com（国外API，可能被墙）
        result = getGeoFromIpApi(ip);
        if (result != null && (Boolean) result.getOrDefault("success", false)) {
            log.info("✅ [GEO] Successfully fetched geo location from ip-api.com for IP: {}", ip);
            return result;
        }

        // 主API失败，尝试备用API1：淘宝IP库（国内API，速度快）
        log.warn("⚠️ Failed to fetch from ip-api.com, trying Taobao IP API...");
        result = getGeoFromTaobao(ip);
        if (result != null && (Boolean) result.getOrDefault("success", false)) {
            log.info("✅ [GEO] Successfully fetched geo location from Taobao IP API for IP: {}", ip);
            return result;
        }

        // 备用API1失败，尝试备用API2：seeip.org
        log.warn("⚠️ Failed to fetch from Taobao IP API, trying seeip.org...");
        result = getGeoFromSeeIp(ip);
        if (result != null && (Boolean) result.getOrDefault("success", false)) {
            log.info("✅ [GEO] Successfully fetched geo location from seeip.org for IP: {}", ip);
            return result;
        }

        // 所有API都失败，返回基础信息
        log.error("❌ Failed to fetch geo location from all APIs for IP: {}", ip);
        result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Failed to fetch geo location from all APIs");
        return result;
    }

    /**
     * 从 ip-api.com 获取地理位置信息
     * API文档: http://ip-api.com/docs/api:json
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    private Map<String, Object> getGeoFromIpApi(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";
            String response = sendHttpGet(url);

            if (response == null || response.isEmpty()) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "";

            if (!"success".equals(status)) {
                // 获取错误信息
                String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "Unknown error";
                log.warn("⚠️ ip-api.com returned non-success status: {}, message: {}, response: {}",
                    status, message, response);
                return null;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ip", ip);
            result.put("country", jsonNode.has("country") ? jsonNode.get("country").asText() : "");
            result.put("region", jsonNode.has("regionName") ? jsonNode.get("regionName").asText() : "");
            result.put("city", jsonNode.has("city") ? jsonNode.get("city").asText() : "");
            result.put("isp", jsonNode.has("isp") ? jsonNode.get("isp").asText() : "");
            result.put("lat", jsonNode.has("lat") ? jsonNode.get("lat").asDouble() : 0.0);
            result.put("lon", jsonNode.has("lon") ? jsonNode.get("lon").asDouble() : 0.0);
            result.put("timezone", jsonNode.has("timezone") ? jsonNode.get("timezone").asText() : "");
            result.put("raw_json", response);

            return result;
        } catch (Exception e) {
            log.error("❌ Error fetching from ip-api.com: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从淘宝IP库获取地理位置信息（国内API，速度快）
     * API文档: https://ip.taobao.com/
     * 注意：淘宝IP库已停止对外服务，这里使用备用的国内IP查询服务
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    private Map<String, Object> getGeoFromTaobao(String ip) {
        try {
            // 使用 ip-api.com 的中国镜像或其他国内IP查询服务
            // 这里使用 whois.pconline.com.cn（太平洋电脑网IP查询）
            String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            // 太平洋IP库返回GBK编码
            String response = sendHttpGetWithEncoding(url, "GBK");

            if (response == null || response.isEmpty()) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ip", ip);

            // 太平洋IP库返回格式：{"ip":"xxx","pro":"省份","city":"城市","region":"地区","addr":"详细地址","err":""}
            String pro = jsonNode.has("pro") ? jsonNode.get("pro").asText() : "";
            String city = jsonNode.has("city") ? jsonNode.get("city").asText() : "";
            String addr = jsonNode.has("addr") ? jsonNode.get("addr").asText() : "";

            result.put("country", "中国");
            result.put("region", pro);
            result.put("city", city);

            // 从addr中提取运营商信息
            String isp = "";
            if (addr.contains("电信")) {
                isp = "中国电信";
            } else if (addr.contains("联通")) {
                isp = "中国联通";
            } else if (addr.contains("移动")) {
                isp = "中国移动";
            } else if (addr.contains("铁通")) {
                isp = "中国铁通";
            } else if (addr.contains("教育网")) {
                isp = "中国教育网";
            }
            result.put("isp", isp);

            result.put("lat", 0.0);
            result.put("lon", 0.0);
            result.put("timezone", "Asia/Shanghai");
            result.put("raw_json", response);

            return result;
        } catch (Exception e) {
            log.error("❌ Error fetching from Taobao IP API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 seeip.org 获取地理位置信息
     * API文档: https://seeip.org/
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    private Map<String, Object> getGeoFromSeeIp(String ip) {
        try {
            String url = "https://api.seeip.org/geoip/" + ip;
            String response = sendHttpGet(url);

            if (response == null || response.isEmpty()) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ip", ip);
            result.put("country", jsonNode.has("country") ? jsonNode.get("country").asText() : "");
            result.put("region", jsonNode.has("region") ? jsonNode.get("region").asText() : "");
            result.put("city", jsonNode.has("city") ? jsonNode.get("city").asText() : "");
            result.put("isp", jsonNode.has("organization") ? jsonNode.get("organization").asText() : "");
            result.put("lat", jsonNode.has("latitude") ? jsonNode.get("latitude").asDouble() : 0.0);
            result.put("lon", jsonNode.has("longitude") ? jsonNode.get("longitude").asDouble() : 0.0);
            result.put("timezone", jsonNode.has("timezone") ? jsonNode.get("timezone").asText() : "");
            result.put("raw_json", response);

            return result;
        } catch (Exception e) {
            log.error("❌ Error fetching from seeip.org: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送HTTP GET请求（UTF-8编码）
     *
     * @param urlString URL地址
     * @return 响应内容
     */
    private String sendHttpGet(String urlString) {
        return sendHttpGetWithEncoding(urlString, StandardCharsets.UTF_8.name());
    }

    /**
     * 发送HTTP GET请求（指定编码）
     *
     * @param urlString URL地址
     * @param encoding 字符编码（如 "UTF-8", "GBK"）
     * @return 响应内容
     */
    private String sendHttpGetWithEncoding(String urlString, String encoding) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Charset", encoding);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                log.warn("⚠️ HTTP request failed with code: {}", responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), encoding)
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            log.error("❌ HTTP request error: {}", e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 判断是否为本地IP
     *
     * @param ip IP地址
     * @return true=本地IP, false=公网IP
     */
    private boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        // 本地回环地址
        if (ip.startsWith("127.") || ip.equals("localhost") || ip.equals("::1")) {
            return true;
        }
        
        // 内网地址
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return true;
        }
        
        // 未知地址
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("unknown")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取模拟地理位置数据（用于本地开发测试）
     * 根据不同的本地IP返回不同的模拟数据
     *
     * @param ip IP地址
     * @return 模拟的地理位置信息
     */
    private Map<String, Object> getMockGeoData(String ip) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ip", ip);
        
        // 根据IP的最后一位数字返回不同的模拟数据
        String lastChar = ip != null && !ip.isEmpty() ? ip.substring(ip.length() - 1) : "0";
        int variant = 0;
        try {
            variant = Integer.parseInt(lastChar) % 5;
        } catch (NumberFormatException e) {
            variant = 0;
        }
        
        // 5种不同的模拟数据
        switch (variant) {
            case 0:
                // 北京
                result.put("country", "中国");
                result.put("region", "北京市");
                result.put("city", "北京市");
                result.put("isp", "中国联通");
                result.put("lat", 39.9042);
                result.put("lon", 116.4074);
                result.put("timezone", "Asia/Shanghai");
                break;
            case 1:
                // 上海
                result.put("country", "中国");
                result.put("region", "上海市");
                result.put("city", "上海市");
                result.put("isp", "中国电信");
                result.put("lat", 31.2304);
                result.put("lon", 121.4737);
                result.put("timezone", "Asia/Shanghai");
                break;
            case 2:
                // 深圳
                result.put("country", "中国");
                result.put("region", "广东省");
                result.put("city", "深圳市");
                result.put("isp", "中国移动");
                result.put("lat", 22.5431);
                result.put("lon", 114.0579);
                result.put("timezone", "Asia/Shanghai");
                break;
            case 3:
                // 杭州
                result.put("country", "中国");
                result.put("region", "浙江省");
                result.put("city", "杭州市");
                result.put("isp", "中国电信");
                result.put("lat", 30.2741);
                result.put("lon", 120.1551);
                result.put("timezone", "Asia/Shanghai");
                break;
            case 4:
                // 成都
                result.put("country", "中国");
                result.put("region", "四川省");
                result.put("city", "成都市");
                result.put("isp", "中国联通");
                result.put("lat", 30.5728);
                result.put("lon", 104.0668);
                result.put("timezone", "Asia/Shanghai");
                break;
        }
        
        // 构建模拟的JSON数据
        String mockJson = String.format(
            "{\"status\":\"success\",\"country\":\"%s\",\"regionName\":\"%s\",\"city\":\"%s\",\"isp\":\"%s\",\"lat\":%.4f,\"lon\":%.4f,\"timezone\":\"%s\",\"query\":\"%s\",\"mock\":true}",
            result.get("country"), result.get("region"), result.get("city"), result.get("isp"),
            result.get("lat"), result.get("lon"), result.get("timezone"), ip
        );
        result.put("raw_json", mockJson);
        
        log.info("🎭 [MOCK] Generated mock geo data for local IP {}: {}-{}-{}", 
                ip, result.get("country"), result.get("region"), result.get("city"));
        
        return result;
    }
}

