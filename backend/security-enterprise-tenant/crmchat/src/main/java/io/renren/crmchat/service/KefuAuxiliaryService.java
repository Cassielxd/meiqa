package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.redis.RedisUtils;
import io.renren.crmchat.dao.*;
import io.renren.crmchat.entity.*;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.FileService;
import org.springframework.web.multipart.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kefu Auxiliary Service - 客服辅助功能服务
 * PHP Reference: /app/controller/kefu/User.php, Statistics.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. logout(): 客服登出
 * 2. getMessageCount(): 获取未读消息数
 * 3. saveFeedback(): 保存客服反馈
 * 4. getUserAgreement(): 获取用户协议
 * 5. getKefuSum(): 客户统计（总数、今日、本月、游客）
 * 6. getKefuMobileStatistics(): 客户首页统计
 * 7. checkVersion(): APP版本检查
 * 8. upload(): 文件上传（TODO: 需要文件存储服务支持）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuAuxiliaryService {

    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatUserMapper chatUserMapper;
    private final ChatServiceFeedbackMapper chatServiceFeedbackMapper;
    private final ApplicationMapper applicationMapper;
    private final FileService fileService;
    private final SystemAttachmentMapper systemAttachmentMapper;
    private final RedisUtils redisUtils;
    private final AutoBadgeService autoBadgeService;

    /**
     * 客服登出
     * POST /api/kefu/logout
     *
     * PHP Reference: User.php::logout()
     *
     * 业务逻辑:
     * 1. 删除Redis中的token（TODO: 需要Redis支持）
     * 2. 更新客服状态：online=0, client_id='', is_app=0, is_backstage=0
     * 3. 更新ChatUser中的online=0
     *
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(Integer kefuId, String currentAppid) {
        // 1. 获取客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer userId = kefu.getUserId();
        if (userId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. TODO: 删除Redis中的token
        // CacheService.redisHandler().delete(key);

        // 3. 更新客服状态
        kefu.setOnline(0);
        kefu.setClientId("");
        kefu.setIsApp(0);
        kefu.setIsBackstage(0);
        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Failed to logout");
        }

        // 4. 注意：ChatUser表中没有online字段，只在ChatService中维护
        // 所以不需要更新ChatUser

        log.info("Agent signed out successfully: kefuId={}, userId={}", kefuId, userId);
    }

    /**
     * 获取未读消息数
     * GET /api/kefu/message/count
     *
     * PHP Reference: User.php::getMessageCount()
     *
     * 业务逻辑:
     * 1. 触发 AutoBadgeService 清除徽章并同步 Redis
     * 2. 统计当前客服的ChatServiceRecord中的mssage_num总和
     *
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 未读消息总数
     */
    public Map<String, Object> getMessageCount(Integer kefuId, String currentAppid) {
        // 1. 获取客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer userId = kefu.getUserId();
        if (userId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        autoBadgeService.dispatch(userId, 0, currentAppid);

        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);
        wrapper.eq("user_id", userId);
        wrapper.select("COALESCE(SUM(mssage_num),0) AS total");
        List<Map<String, Object>> rows = chatServiceRecordMapper.selectMaps(wrapper);
        int total = 0;
        if (!rows.isEmpty()) {
            Object value = rows.get(0).get("total");
            if (value instanceof Number number) {
                total = number.intValue();
            } else if (value != null) {
                try {
                    total = Integer.parseInt(value.toString());
                } catch (NumberFormatException ignored) {
                    total = 0;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", total);

        return result;
    }

    /**
     * 保存客服反馈
     * POST /api/kefu/feedback
     *
     * PHP Reference: User.php::saveFeedback()
     *
     * 业务逻辑:
     * 1. 验证必填字段：rela_name, phone, content
     * 2. 内容htmlspecialchars处理（Java中使用escapeHtml）
     * 3. 保存到ChatServiceFeedback表
     *
     * @param data         反馈数据
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFeedback(Map<String, Object> data, Integer kefuId, String currentAppid) {
        // 1. 获取客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Support agent not found.");
        }

        Integer userId = kefu.getUserId();
        if (userId == null) {
            throw new CrmChatException("Support user account not found.");
        }

        // 2. 验证必填字段
        if (!data.containsKey("rela_name") || data.get("rela_name") == null || data.get("rela_name").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter contact name");
        }
        if (!data.containsKey("phone") || data.get("phone") == null || data.get("phone").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter contact phone");
        }
        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter feedback content");
        }

        String relaName = data.get("rela_name").toString();
        String phone = data.get("phone").toString();
        String content = data.get("content").toString();

        // 3. 内容处理（简化版，Java中使用StringEscapeUtils或直接保存）
        // content = htmlspecialchars(content);

        // 4. 创建反馈记录
        ChatServiceFeedbackEntity feedback = new ChatServiceFeedbackEntity();
        feedback.setUserId(userId);
        feedback.setTitle(relaName); // PHP中的rela_name对应title字段
        feedback.setContent(content);
        feedback.setAddTime((int) (System.currentTimeMillis() / 1000));
        feedback.setAppid(currentAppid);
        feedback.setStatus(0); // 未处理

        // 注意：ChatServiceFeedback表可能没有phone和rela_name字段
        // 如果有make字段，可以将联系方式存入make
        feedback.setMake("Contact: " + relaName + ", Phone: " + phone);

        int result = chatServiceFeedbackMapper.insert(feedback);
        if (result <= 0) {
            throw new CrmChatException("Failed to save");
        }

        log.info("Saved agent feedback successfully: kefuId={}, userId={}", kefuId, userId);
    }

    /**
     * 获取用户协议
     * GET /api/kefu/agreement
     *
     * PHP Reference: User.php::getUserAgreement()
     *
     * 业务逻辑:
     * 1. 从缓存或数据库读取user_agreement配置
     * 2. 返回协议内容
     *
     * @return 用户协议内容
     */
    public Map<String, Object> getUserAgreement() {
        // TODO: 从系统配置表或缓存读取user_agreement
        // $cache->getDbCache('user_agreement', '');

        Map<String, Object> result = new HashMap<>();
        result.put("content", ""); // 默认返回空内容

        // TODO: 实现系统配置读取逻辑
        // String content = systemConfigService.getConfig("user_agreement", "");
        // result.put("content", content);

        return result;
    }

    /**
     * 客户统计
     * GET /api/kefu/statistics/sum
     *
     * PHP Reference: Statistics.php::sum() -> ChatUserServices::getKefuSum()
     *
     * 业务逻辑:
     * 1. all - 所有客户总数
     * 2. toDayKefu - 今日新增客户数（非游客）
     * 3. month - 本月客户总数
     * 4. toDayTourist - 今日游客数
     *
     * @param currentAppid 当前租户appid
     * @return 客户统计数据
     */
    public Map<String, Object> getKefuSum(String currentAppid) {
        QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);

        // 1. 总数
        Long all = chatUserMapper.selectCount(wrapper);

        // 2. 今日新增客户（非游客）
        QueryWrapper<ChatUserEntity> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("appid", currentAppid);
        todayWrapper.eq("is_tourist", 0);
        // 今日：add_time >= 今天0点的时间戳
        int todayStartTime = (int) (System.currentTimeMillis() / 1000 / 86400 * 86400); // 今天0点
        todayWrapper.ge("add_time", todayStartTime);
        Long toDayKefu = chatUserMapper.selectCount(todayWrapper);

        // 3. 本月客户总数
        QueryWrapper<ChatUserEntity> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("appid", currentAppid);
        // 本月：add_time >= 本月1号0点的时间戳
        // 简化实现：这里使用近30天
        int monthStartTime = (int) (System.currentTimeMillis() / 1000 - 30 * 86400);
        monthWrapper.ge("add_time", monthStartTime);
        Long month = chatUserMapper.selectCount(monthWrapper);

        // 4. 今日游客数
        QueryWrapper<ChatUserEntity> touristWrapper = new QueryWrapper<>();
        touristWrapper.eq("appid", currentAppid);
        touristWrapper.eq("is_tourist", 1);
        touristWrapper.ge("add_time", todayStartTime);
        Long toDayTourist = chatUserMapper.selectCount(touristWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("all", all != null ? all.intValue() : 0);
        result.put("toDayKefu", toDayKefu != null ? toDayKefu.intValue() : 0);
        result.put("month", month != null ? month.intValue() : 0);
        result.put("toDayTourist", toDayTourist != null ? toDayTourist.intValue() : 0);

        return result;
    }

    /**
     * 客户首页统计（移动端）
     * GET /api/kefu/statistics/index
     *
     * PHP Reference: Statistics.php::index() -> ChatUserServices::getKefuMobileStatistics()
     *
     * 业务逻辑:
     * 1. 根据time参数统计不同时间段的数据
     * 2. time可选值：today, week, month
     * 3. 返回客户增长趋势数据
     *
     * @param time         时间范围（today/week/month）
     * @param currentAppid 当前租户appid
     * @return 统计数据
     */
    public Map<String, Object> getKefuMobileStatistics(String time, String currentAppid) {
        // TODO: 实现详细的统计逻辑
        // 这里提供简化版实现

        Map<String, Object> result = new HashMap<>();

        // 根据time参数确定时间范围
        int startTime = 0;
        if ("today".equals(time)) {
            startTime = (int) (System.currentTimeMillis() / 1000 / 86400 * 86400);
        } else if ("week".equals(time)) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 7 * 86400);
        } else if ("month".equals(time)) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 30 * 86400);
        }

        // 统计数据
        QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);
        if (startTime > 0) {
            wrapper.ge("add_time", startTime);
        }
        Long count = chatUserMapper.selectCount(wrapper);

        result.put("count", count != null ? count.intValue() : 0);
        result.put("time", time);

        return result;
    }

    /**
     * APP版本检查
     * POST /api/kefu/version
     *
     * PHP Reference: Service.php::version()
     *
     * 业务逻辑:
     * 1. 根据version参数查询eb_application表
     * 2. 如果存在更新版本，返回更新信息
     * 3. 如果不存在，返回update=false
     *
     * @param version 当前APP版本号
     * @return 版本信息
     */
    public Map<String, Object> checkVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("update", false);
            return result;
        }

        // 查询应用版本表
        // 注意：eb_application表可能是用于其他用途的，APP版本管理可能在另一个表
        // 这里提供简化实现，实际需要根据表结构调整
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("name", "Customer Service APP"); // 假设通过name字段标识APP
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT 1");

        ApplicationEntity app = applicationMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        if (app != null && app.getIntroduce() != null && !app.getIntroduce().isEmpty()) {
            // 假设introduce字段包含版本信息
            result.put("update", true);
            result.put("version", "1.0.1"); // TODO: 从数据库读取实际版本号
            result.put("url", ""); // TODO: 下载地址
            result.put("name", app.getName());
            result.put("info", app.getIntroduce());
        } else {
            result.put("update", false);
        }

        return result;
    }

    /**
     * 文件上传
     * POST /api/kefu/upload
     *
     * PHP Reference: User.php::upload()
     *
     * 业务逻辑:
     * 1. 验证上传频率（限制每天100次）
     * 2. 使用UploadService处理文件上传
     * 3. 保存附件信息到SystemAttachment表
     * 4. 返回文件URL
     *
     * @param kefuId 当前客服ID
     * @return 上传结果
     */
    public Map<String, Object> upload(Integer kefuId, String currentAppid, MultipartFile file) {

        if (kefuId == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }
        if (file == null || file.isEmpty()) {
            throw new CrmChatException("Invalid parameters");
        }

        ChatServiceEntity kefuUpload = chatServiceMapper.selectById(kefuId);
        if (kefuUpload == null || !kefuUpload.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer kefuUserId = kefuUpload.getUserId();
        if (kefuUserId == null) {
            throw new CrmChatException("Customer service user does not exist");
        }

        String counterKey = "crmchat:kefu:uploads:" + kefuUserId;
        int currentCount = parseCounter(redisUtils.get(counterKey, RedisUtils.DEFAULT_EXPIRE));
        if (currentCount >= 100) {
            throw new CrmChatException("Illegal operation");
        }

        String fileUrl = fileService.uploadFile(file, currentAppid, "kefu");
        int now = (int) (System.currentTimeMillis() / 1000);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "upload_" + now;
        }
        long fileSize = file.getSize();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        // PHP逻辑: 确保URL包含完整域名
        // $res['dir'] = path_to_url($res['dir']);
        // if (strpos($res['dir'], 'http') === false) $res['dir'] = $request->domain() . $res['dir'];
        String completeUrl = fileUrl;
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            // 如果fileUrl不包含http协议，使用getFileUrl确保返回完整URL
            completeUrl = fileService.getFileUrl(fileUrl);
        }

        SystemAttachmentEntity attachment = new SystemAttachmentEntity();
        attachment.setName(originalFilename);
        attachment.setRealName(originalFilename);
        attachment.setAttDir(completeUrl);
        attachment.setSattDir(completeUrl);
        attachment.setAttSize(String.valueOf(fileSize));
        attachment.setAttType(contentType);
        attachment.setImageType(1);
        attachment.setModuleType(1);
        attachment.setPid(0);
        attachment.setTime(now);
        systemAttachmentMapper.insert(attachment);

        redisUtils.set(counterKey, currentCount + 1, RedisUtils.DEFAULT_EXPIRE);

        Map<String, Object> result = new HashMap<>();
        result.put("name", originalFilename);
        result.put("url", completeUrl);
        result.put("att_id", attachment.getAttId());
        result.put("att_size", attachment.getAttSize());
        result.put("att_type", attachment.getAttType());
        autoBadgeService.dispatch(kefuUserId, 0, currentAppid);
        return result;
    }

    private int parseCounter(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
