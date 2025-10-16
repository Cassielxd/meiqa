package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ApplicationMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import io.renren.common.utils.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

/**
 * Tenant 搴旂敤绠＄悊鏈嶅姟
 * PHP Reference: /app/controller/tenant/Application.php
 *
 * 鏍稿績涓氬姟閫昏緫锛堜弗鏍煎弬鑰働HP锛?
 * 1. getApplication(): 鑾峰彇搴旂敤淇℃伅
 *    - 鏍规嵁appid鏌ヨ
 * 2. createApplication(): 鍒涘缓搴旂敤
 *    - 楠岃瘉蹇呭～瀛楁
 *    - 鐢熸垚appid銆乤pp_secret銆乼oken
 *    - 妫€鏌ュ悕绉板敮涓€鎬?
 * 3. updateApplication(): 鏇存柊搴旂敤
 *    - 楠岃瘉蹇呭～瀛楁
 *    - 鏇存柊鍩烘湰淇℃伅
 * 4. deleteApplication(): 鍒犻櫎搴旂敤
 *    - 杞垹闄わ紙is_delete=1锛?
 * 5. resetToken(): 閲嶇疆token
 *    - 閲嶆柊鐢熸垚app_secret鍜宼oken
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantApplicationService {

    private final ApplicationMapper applicationMapper;

    /**
     * 鑾峰彇搴旂敤淇℃伅
     * GET /api/tenant/app
     *
     * PHP Reference: Application.php::index()
     *
     * 涓氬姟閫昏緫:
     * 1. 鏍规嵁appid鏌ヨ搴旂敤
     * 2. 杩斿洖搴旂敤淇℃伅
     *
     * @param appid 绉熸埛appid
     * @return 搴旂敤淇℃伅
     */
    public ApplicationEntity getApplication(String appid) {
        // PHP: $where["appid"] = $appid;
        // PHP: return $this->success("Query succeeded", $this->services->getOne($where)->toArray());

        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("is_delete", 0);

        ApplicationEntity application = applicationMapper.selectOne(wrapper);
        if (application == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        return application;
    }

    /**
     * 鍒涘缓搴旂敤
     * POST /api/tenant/app
     *
     * PHP Reference: Application.php::save()
     *
     * 涓氬姟閫昏緫:
     * 1. 楠岃瘉icon鍜宯ame闈炵┖
     * 2. 妫€鏌ame鍞竴鎬?
     * 3. 鐢熸垚appid锛堝勾浠?鏃堕棿鎴?闅忔満鏁帮級
     * 4. 鐢熸垚app_secret鍜宼oken
     * 5. 淇濆瓨鍒版暟鎹簱
     *
     * @param data 搴旂敤鏁版嵁锛坕con, name, introduce锛?
     * @return 鏂板垱寤虹殑搴旂敤淇℃伅
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationEntity createApplication(Map<String, Object> data) {
        // 1. PHP: if (!$data['icon']) return $this->fail('璇烽€夋嫨搴旂敤鍥炬爣');
        if (!data.containsKey("icon") || data.get("icon") == null || data.get("icon").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select application icon");
        }

        // 2. PHP: if (!$data['name']) return $this->fail('璇峰～鍐欏簲鐢ㄥ悕绉?);
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter application name");
        }

        String name = data.get("name").toString();

        // 3. PHP: if ($this->services->count(['name' => $data['name']])) return $this->fail('搴旂敤鍚嶇О宸插瓨鍦?);
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_delete", 0);
        Long count = applicationMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Application name already exists");
        }

        // 4. PHP: 鐢熸垚appid鍜宼oken
        Random random = new Random();
        int rand = random.nextInt(9000) + 1000; // 1000-9999
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String appid = year + String.valueOf(timestamp) + rand;
        String appSecret = DigestUtils.md5DigestAsHex((appid + timestamp + rand).getBytes());

        // 绠€鍖栫増token鐢熸垚锛圥HP浣跨敤Encrypter锛岃繖閲屼娇鐢∕D5锛?
        String token = DigestUtils.md5DigestAsHex((appid + appSecret + rand + timestamp).getBytes());
        String tokenMd5 = DigestUtils.md5DigestAsHex(token.getBytes());

        // 5. 鍒涘缓搴旂敤璁板綍
        ApplicationEntity application = new ApplicationEntity();
        application.setAppid(appid);
        application.setIcon(data.get("icon").toString());
        application.setName(name);

        if (data.containsKey("introduce") && data.get("introduce") != null) {
            application.setIntroduce(data.get("introduce").toString());
        }

        application.setRand(rand);
        application.setTimestamp(timestamp);
        application.setAppSecret(appSecret);
        application.setToken(token);
        application.setTokenMd5(tokenMd5);
        application.setIsDelete(0);

        int result = applicationMapper.insert(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save");
        }

        return application;
    }

    /**
     * 鏇存柊搴旂敤
     * PUT /api/tenant/app/:id
     *
     * PHP Reference: Application.php::update()
     *
     * 涓氬姟閫昏緫:
     * 1. 楠岃瘉icon鍜宯ame闈炵┖
     * 2. 鏇存柊搴旂敤淇℃伅
     *
     * @param id   搴旂敤ID
     * @param data 搴旂敤鏁版嵁锛坕con, name, introduce锛?
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['icon']) return $this->fail('璇烽€夋嫨搴旂敤鍥炬爣');
        if (!data.containsKey("icon") || data.get("icon") == null || data.get("icon").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select application icon");
        }

        // 2. PHP: if (!$data['name']) return $this->fail('璇峰～鍐欏簲鐢ㄥ悕绉?);
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter application name");
        }

        // 鏌ヨ搴旂敤鏄惁瀛樺湪
        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null || application.getIsDelete() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        // 3. PHP: $this->services->update($id, $data);
        application.setIcon(data.get("icon").toString());
        application.setName(data.get("name").toString());

        if (data.containsKey("introduce") && data.get("introduce") != null) {
            application.setIntroduce(data.get("introduce").toString());
        }

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save");
        }
    }

    /**
     * 鍒犻櫎搴旂敤
     * DELETE /api/tenant/app/:id
     *
     * PHP Reference: Application.php::delete()
     *
     * 涓氬姟閫昏緫:
     * 1. 杞垹闄ゅ簲鐢紙is_delete=1锛?
     *
     * @param id 搴旂敤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Integer id) {
        // PHP: $this->services->update($id, ['is_delete' => 1]);

        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        application.setIsDelete(1);

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 閲嶇疆token
     * PUT /api/tenant/app/reset/:id
     *
     * PHP Reference: Application.php::reset()
     *
     * 涓氬姟閫昏緫:
     * 1. 楠岃瘉搴旂敤瀛樺湪
     * 2. 閲嶆柊鐢熸垚rand銆乼imestamp銆乤pp_secret銆乼oken
     * 3. 鏇存柊鍒版暟鎹簱
     * 4. 杩斿洖鏂扮殑token淇℃伅
     *
     * @param id 搴旂敤ID
     * @return 鏂扮殑token淇℃伅
     */
    @Transactional(rollbackFor = Exception.class)
    public String resetToken(Integer id) {
        // 1. PHP: $appInfo = $this->services->get($id);
        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null || application.getIsDelete() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        // 2. PHP: 閲嶆柊鐢熸垚token鐩稿叧瀛楁
        Random random = new Random();
        int rand = random.nextInt(9000) + 1000;
        int timestamp = (int) (System.currentTimeMillis() / 1000);

        String appSecret = DigestUtils.md5DigestAsHex((application.getAppid() + timestamp + rand).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> tokenPayload = Map.of(
                "appid", application.getAppid(),
                "app_secret", appSecret,
                "rand", rand,
                "timestamp", timestamp
        );
        String tokenJson = JsonUtils.toJsonString(tokenPayload);
        String token = Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8));
        String tokenMd5 = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));

        // 3. 鏇存柊鍒版暟鎹簱
        application.setRand(rand);
        application.setTimestamp(timestamp);
        application.setAppSecret(appSecret);
        application.setToken(token);
        application.setTokenMd5(tokenMd5);

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to reset");
        }

        // 4. 杩斿洖鏂扮殑token淇℃伅
        return application.getAppid();
    }
}


