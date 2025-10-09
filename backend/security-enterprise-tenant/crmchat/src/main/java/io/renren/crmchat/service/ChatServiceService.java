package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.PasswordService;
import io.renren.crmchat.service.common.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 客服服务
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class ChatServiceService {

    private final ChatServiceMapper chatServiceMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    /**
     * 客服登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录信息（token + kefu_info）
     */
    public Map<String, Object> login(String account, String password) {
        // 1. 验证登录
        ChatServiceEntity kefuInfo = verifyLogin(account, password);

        // 2. 生成 JWT Token（使用 TokenService，传入客服所属租户的 appid）
        String token = tokenService.generateKefuToken(
                Long.valueOf(kefuInfo.getId()),
                kefuInfo.getAccount(),
                kefuInfo.getAppid()  // 使用客服所属租户的 appid
        );

        // 3. 返回登录信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> kefuInfoMap = new HashMap<>();
        kefuInfoMap.put("id", kefuInfo.getId());
        kefuInfoMap.put("uid", kefuInfo.getUserId());
        kefuInfoMap.put("appid", kefuInfo.getAppid());
        kefuInfoMap.put("group_id", kefuInfo.getGroupId());
        kefuInfoMap.put("nickname", kefuInfo.getNickname());
        kefuInfoMap.put("account", kefuInfo.getAccount());
        kefuInfoMap.put("phone", kefuInfo.getPhone());
        kefuInfoMap.put("avatar", kefuInfo.getAvatar());
        kefuInfoMap.put("welcome_words", kefuInfo.getWelcomeWords());
        kefuInfoMap.put("auto_reply", kefuInfo.getAutoReply());
        kefuInfoMap.put("status", kefuInfo.getStatus());
        result.put("kefu_info", kefuInfoMap);

        return result;
    }

    /**
     * 扫码登录场景：根据客服ID直接生成登录信息
     */
    public Map<String, Object> loginById(Integer kefuId) {
        if (kefuId == null) {
            throw new CrmChatException("Missing customer service ID");
        }

        ChatServiceEntity kefuInfo = chatServiceMapper.selectById(kefuId);
        if (kefuInfo == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        if (kefuInfo.getStatus() == 0) {
            throw new CrmChatException("Customer service account has been disabled");
        }

        kefuInfo.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        kefuInfo.setOnline(1);
        chatServiceMapper.updateById(kefuInfo);

        String token = tokenService.generateKefuToken(
                Long.valueOf(kefuInfo.getId()),
                kefuInfo.getAccount(),
                kefuInfo.getAppid()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> kefuInfoMap = new HashMap<>();
        kefuInfoMap.put("id", kefuInfo.getId());
        kefuInfoMap.put("uid", kefuInfo.getUserId());
        kefuInfoMap.put("appid", kefuInfo.getAppid());
        kefuInfoMap.put("group_id", kefuInfo.getGroupId());
        kefuInfoMap.put("nickname", kefuInfo.getNickname());
        kefuInfoMap.put("account", kefuInfo.getAccount());
        kefuInfoMap.put("phone", kefuInfo.getPhone());
        kefuInfoMap.put("avatar", kefuInfo.getAvatar());
        kefuInfoMap.put("welcome_words", kefuInfo.getWelcomeWords());
        kefuInfoMap.put("auto_reply", kefuInfo.getAutoReply());
        kefuInfoMap.put("status", kefuInfo.getStatus());
        result.put("kefu_info", kefuInfoMap);

        return result;
    }

    /**
     * 验证客服登录
     *
     * @param account  账号
     * @param password 密码
     * @return 客服信息
     */
    private ChatServiceEntity verifyLogin(String account, String password) {
        // 1. 查询客服
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        wrapper.eq("status", 1); // 只查询启用状态的客服
        wrapper.last("LIMIT 1"); // 如果有多条记录，只取第一条
        ChatServiceEntity kefuInfo = chatServiceMapper.selectOne(wrapper);

        if (kefuInfo == null) {
            throw new CrmChatException("Customer service agent does not exist or has been disabled");
        }

        // 2. 检查状态
        if (kefuInfo.getStatus() == 0) {
            throw new CrmChatException("Customer service account has been disabled");
        }

        // 3. 验证密码（使用 PasswordService，自动处理 PHP BCrypt 兼容性）
        passwordService.verifyOrThrow(
                password,
                kefuInfo.getPassword(),
                "Incorrect account or password, please try again"
        );

        // 4. 更新登录信息
        kefuInfo.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        kefuInfo.setOnline(1);  // 设置为在线状态
        chatServiceMapper.updateById(kefuInfo);

        return kefuInfo;
    }

    /**
     * 获取登录页信息
     *
     * @return 登录页配置
     */
    public Map<String, Object> getLoginInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("logo_square", "/logo.png");
        info.put("logo_rectangle", "/logo_rectangle.png");
        info.put("login_logo", "/login_logo.png");
        info.put("slide", new String[]{});
        info.put("site_name", "CRMChat Customer Service System");
        return info;
    }

    /**
     * 获取客服信息
     * PHP Reference: User.php::getKefuInfo()
     *
     * @param kefuId 客服ID
     * @return 客服信息
     */
    public Map<String, Object> getKefuInfo(Integer kefuId) {
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", kefu.getId());
        result.put("uid", kefu.getUserId());
        result.put("appid", kefu.getAppid());
        result.put("group_id", kefu.getGroupId());
        result.put("nickname", kefu.getNickname());
        result.put("account", kefu.getAccount());
        result.put("phone", kefu.getPhone());
        result.put("avatar", kefu.getAvatar());
        result.put("welcome_words", kefu.getWelcomeWords());
        result.put("auto_reply", kefu.getAutoReply());
        result.put("status", kefu.getStatus());
        result.put("online", kefu.getOnline());
        result.put("password", "******");  // 密码脱敏
        result.put("site_title", "CRMChat");  // TODO: 从系统配置获取

        // TODO: 获取当前appid下所有客服的user_id列表
        // result.put("user_ids", services.getColumn(['appid' => kefuInfo['appid']], 'user_id'));

        return result;
    }

    /**
     * 更新客服个人信息
     * PHP Reference: User.php::updateKefu()
     *
     * @param kefuId 客服ID
     * @param data   更新数据
     */
    public void updateKefuProfile(Integer kefuId, Map<String, Object> data) {
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        String nickname = (String) data.get("nickname");
        String avatar = (String) data.get("avatar");
        String phone = (String) data.get("phone");

        // 更新客服表
        if (nickname != null && !nickname.trim().isEmpty()) {
            kefu.setNickname(nickname);
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            kefu.setAvatar(avatar);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            kefu.setPhone(phone);
        }

        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        chatServiceMapper.updateById(kefu);

        // TODO: 同步更新ChatServiceRecord表的nickname和avatar
        // this.services.update(['to_user_id' => $this->kefuId], $update);

        // TODO: 同步更新ChatUser表的nickname和avatar
        // $userServices.update(['id' => $this->kefuInfo['user_id']], $update);
    }

    /**
     * 修改客服密码
     * PHP Reference: User.php::updateKefu() (password字段)
     *
     * @param kefuId      客服ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void updateKefuPassword(Integer kefuId, String oldPassword, String newPassword) {
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        // 验证旧密码
        passwordService.verifyOrThrow(
                oldPassword,
                kefu.getPassword(),
                "Old password is incorrect"
        );

        // 更新为新密码（使用BCrypt加密）
        String encryptedPassword = passwordService.hash(newPassword);
        kefu.setPassword(encryptedPassword);
        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        chatServiceMapper.updateById(kefu);
    }
}
