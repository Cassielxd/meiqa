package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.SystemAdminMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.PasswordService;
import io.renren.crmchat.service.common.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 Service
 * 参考 PHP: SystemAdminServices
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class SystemAdminService {

    private final SystemAdminMapper systemAdminMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final SystemMenusService systemMenusService;

    /**
     * 管理员登录
     * 参考 PHP: SystemAdminServices->login()
     *
     * @param account  账号
     * @param password 密码
     * @return 登录信息（token、用户信息等）
     */
    public Map<String, Object> login(String account, String password) {
        // 1. 验证登录
        SystemAdminEntity adminInfo = verifyLogin(account, password);

        // 2. 生成 Token（使用 TokenService）
        String token = tokenService.generateAdminToken(
                Long.valueOf(adminInfo.getId()),
                adminInfo.getAccount()
        );

        // 3. 获取Token过期时间
        Long expiresTime = tokenService.getTokenExpireAt(token);

        // 4. 返回登录信息（参考PHP返回结构）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expires_time", expiresTime);

        // 用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", adminInfo.getId());
        userInfo.put("account", adminInfo.getAccount());
        userInfo.put("head_pic", adminInfo.getHeadPic() != null ? adminInfo.getHeadPic() : "");
        userInfo.put("real_name", adminInfo.getRealName() != null ? adminInfo.getRealName() : "admin");
        result.put("user_info", userInfo);

        // 获取菜单和权限（参考PHP: SystemMenusServices->getMenusList()）
        SystemMenusService.MenusResult menusResult = systemMenusService.getMenusList(
                adminInfo.getRoles(),
                adminInfo.getLevel()
        );
        result.put("menus", menusResult.getMenus());
        result.put("unique_auth", menusResult.getUniqueAuth());

        // 系统配置
        result.put("logo", "");
        result.put("logo_square", "");
        result.put("version", "4.7.0");
        result.put("newOrderAudioLink", "");

        return result;
    }

    /**
     * 验证登录
     * 参考 PHP: SystemAdminServices->verifyLogin()
     *
     * @param account  账号
     * @param password 密码
     * @return 管理员信息
     */
    private SystemAdminEntity verifyLogin(String account, String password) {
        // 1. 查询管理员
        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        wrapper.eq("is_del", 0);
        SystemAdminEntity adminInfo = systemAdminMapper.selectOne(wrapper);

        if (adminInfo == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        // 2. 检查状态
        if (adminInfo.getStatus() == 0) {
            throw new CrmChatException("You have been banned from logging in");
        }

        // 3. 验证密码（使用 PasswordService，自动处理 PHP BCrypt 兼容性）
        passwordService.verifyOrThrow(
                password,
                adminInfo.getPwd(),
                "Incorrect account or password, please try again"
        );

        // 4. 更新登录信息
        adminInfo.setLastTime((int) (System.currentTimeMillis() / 1000));
        adminInfo.setLastIp(getClientIp());
        adminInfo.setLoginCount(adminInfo.getLoginCount() + 1);
        systemAdminMapper.updateById(adminInfo);

        return adminInfo;
    }

    /**
     * 获取登录页信息
     * 参考 PHP: SystemAdminServices->getLoginInfo()
     *
     * @return 登录页信息
     */
    public Map<String, Object> getLoginInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("logo_square", ""); // 透明logo
        info.put("logo_rectangle", ""); // 方形logo
        info.put("login_logo", ""); // 登录logo
        info.put("slide", new java.util.ArrayList<>()); // 轮播图数组（前端需要）
        info.put("site_name", "CRMChat Customer Service System");
        return info;
    }

    /**
     * 重置管理员密码（临时方法用于测试）
     */
    public void resetAdminPassword(String account, String newPassword) {
        // 1. 查询管理员
        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        wrapper.eq("is_del", 0);
        SystemAdminEntity adminInfo = systemAdminMapper.selectOne(wrapper);

        if (adminInfo == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        // 2. 加密新密码
        String encryptedPassword = passwordService.hash(newPassword);

        // 3. 更新密码
        adminInfo.setPwd(encryptedPassword);
        systemAdminMapper.updateById(adminInfo);
    }

    /**
     * 获取客户端IP（简化版）
     */
    private String getClientIp() {
        // TODO: 从 HttpServletRequest 获取真实IP
        return "127.0.0.1";
    }
}
