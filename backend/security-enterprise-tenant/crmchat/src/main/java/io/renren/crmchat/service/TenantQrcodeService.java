package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import io.renren.crmchat.mapper.QrcodeMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.QrcodeEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tenant 二维码服务
 * PHP Reference: /app/controller/tenant/chat/Qrcode.php
 * PHP Service: /app/services/other/QrcodeServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getQrcodeList(): 获取二维码列表
 *    - 支持name模糊查询
 *    - 关联查询客服账号信息（user_account）
 *    - 返回列表和总数
 * 2. saveQrcode(): 保存/更新二维码
 *    - 验证name非空
 *    - user_ids为客服ID数组
 *    - sort排序字段
 * 3. deleteQrcode(): 删除二维码
 *    - 验证id存在
 *    - 删除记录
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantQrcodeService {

    private final QrcodeMapper qrcodeMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final FormBuilder formBuilder;

    /**
     * 获取二维码列表
     * GET /api/tenant/chat/qrcode
     *
     * PHP Reference:
     * - Controller: Qrcode.php::index()
     * - Service: QrcodeServices.php::getList()
     *
     * 业务逻辑:
     * 1. 支持name模糊查询
     * 2. 查询客服账号信息并关联到user_account字段
     * 3. 返回list和count
     *
     * @param name         二维码名称（模糊查询）
     * @return Map包含list和count
     */
    public Map<String, Object> getQrcodeList(String name) {
        // PHP: $where = $this->request->getMore([['name', '']]);
        // PHP: return $this->success($this->services->getList($where));

        QueryWrapper<QrcodeEntity> wrapper = new QueryWrapper<>();

        // PHP: 模糊查询name
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name);
        }

        wrapper.orderByDesc("sort");
        wrapper.orderByDesc("id");

        List<QrcodeEntity> list = qrcodeMapper.selectList(wrapper);

        // PHP: 获取所有user_ids并查询客服账号
        // foreach ($list as &$item) {
        //     if ($item['user_ids']) {
        //         $userIds = array_merge($userIds, $item['user_ids']);
        //     }
        // }
        Set<Integer> allUserIds = new HashSet<>();
        for (QrcodeEntity item : list) {
            if (item.getUserIds() != null && !item.getUserIds().isEmpty()) {
                allUserIds.addAll(item.getUserIds());
            }
        }

        // PHP: 查询客服账号
        // $kefuList = $service->getColumn(['id' => $userIds], 'account', 'id');
        Map<Integer, String> kefuAccountMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            QueryWrapper<ChatServiceEntity> kefuWrapper = new QueryWrapper<>();
            kefuWrapper.in("id", allUserIds);
            kefuWrapper.select("id", "account");

            List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(kefuWrapper);
            for (ChatServiceEntity kefu : kefuList) {
                kefuAccountMap.put(kefu.getId(), kefu.getAccount());
            }
        }

        // PHP: 关联客服账号到user_account字段
        // foreach ($list as &$item) {
        //     $item['user_account'] = [];
        //     foreach ($kefuList as $id => $account) {
        //         if (in_array($id, $item['user_ids'])) {
        //             $item['user_account'][] = $account;
        //         }
        //     }
        // }
        for (QrcodeEntity item : list) {
            List<String> userAccounts = new ArrayList<>();
            if (item.getUserIds() != null) {
                for (Integer userId : item.getUserIds()) {
                    String account = kefuAccountMap.get(userId);
                    if (account != null) {
                        userAccounts.add(account);
                    }
                }
            }
            item.setUserAccount(userAccounts);
        }

        // PHP: $count = $this->dao->count($where);
        // PHP: return compact('list', 'count');
        long count = qrcodeMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);
        return result;
    }

    /**
     * 获取二维码表单配置
     * GET /api/tenant/chat/qrcode/:id
     *
     * PHP Reference:
     * - Controller: Qrcode.php::create()
     * - Service: QrcodeServices.php::getForm()
     *
     * 业务逻辑:
     * 1. 如果id>0，获取现有二维码数据
     * 2. 获取客服列表作为下拉选项
     * 3. 使用FormBuilder构建表单配置
     * 4. 返回表单配置JSON
     *
     * @param appid 应用ID
     * @param id    二维码ID（0表示创建新的）
     * @return FormBuilder表单配置
     */
    public Map<String, Object> getForm(String appid, Integer id) {
        // PHP: $codeInfo = [];
        // PHP: if ($id) {
        //     $codeInfo = $this->dao->get($id);
        //     if (!$codeInfo) {
        //         throw new ValidateException('修改的二维码不存在');
        //     }
        //     $codeInfo = $codeInfo->toArray();
        // }

        String name = "";
        List<Integer> userIds = new ArrayList<>();
        Integer sort = 0;

        if (id != null && id > 0) {
            QrcodeEntity qrcode = qrcodeMapper.selectById(id);
            if (qrcode == null) {
                throw new CrmChatException("QR code to be modified does not exist");
            }
            TenantGuard.ensureOwnedByCurrentTenant(qrcode.getAppid(), "QR code does not exist");

            name = qrcode.getName() != null ? qrcode.getName() : "";
            userIds = qrcode.getUserIds() != null ? qrcode.getUserIds() : new ArrayList<>();
            sort = qrcode.getSort() != null ? qrcode.getSort() : 0;
        }

        // PHP: $service = app()->make(ChatServiceServices::class);
        // PHP: $data = $service->getKefuSelect(['appid' => $appid]);

        // 获取客服列表作为下拉选项
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1); // 只获取启用的客服
        wrapper.select("id", "nickname", "account");

        List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);

        // 构建options数组
        List<Map<String, Object>> options = new ArrayList<>();
        for (ChatServiceEntity kefu : kefuList) {
            Map<String, Object> option = new HashMap<>();
            option.put("label", kefu.getNickname() != null ? kefu.getNickname() : kefu.getAccount());
            option.put("value", kefu.getId());
            options.add(option);
        }

        // PHP: $rule = [
        //     FormBuilder::input('name', '二维码名称', $codeInfo['name'] ?? '')->required(),
        //     FormBuilder::select('user_ids', '选择客服', $codeInfo['user_ids'] ?? [])
        //         ->required()->options($data)->multiple(true),
        //     FormBuilder::number('sort', '排序', $codeInfo['sort'] ?? 0),
        // ];

        // 使用FormBuilder构建表单组件
        List<BaseComponent> components = new ArrayList<>();

        // input字段：name
        components.add(
            formBuilder.input("name", "二维码名称", name)
                .required()
        );

        // select字段：user_ids (多选客服)
        components.add(
            formBuilder.select("user_ids", "选择客服", userIds)
                .options(options)
                .multiple()
                .required()
        );

        // number字段：sort
        components.add(
            formBuilder.inputNumber("sort", "排序", sort)
        );

        // PHP: return create_form($id ? '编辑二维码' : '添加二维码', $rule, '/chat/qrcode/' . $id);

        String title = (id != null && id > 0) ? "编辑二维码" : "添加二维码";
        String action = "/api/tenant/chat/qrcode/" + (id != null && id > 0 ? id : 0);

        return FormHelper.createForm(title, components, action, "POST");
    }

    /**
     * 保存/更新二维码
     * POST /api/tenant/chat/qrcode/:id
     *
     * PHP Reference:
     * - Controller: Qrcode.php::save()
     * - Service: QrcodeServices.php::saveQrcode()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. user_ids为客服ID数组
     * 3. id存在则更新，否则创建
     *
     * @param id           二维码ID（null表示创建）
     * @param data         二维码数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQrcode(Integer id, Map<String, Object> data) {
        // PHP: $data = $this->request->postMore([
        //     ['name', ''],
        //     ['user_ids', []],
        //     ['sort', 0]
        // ]);

        // 验证name
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter QR code name");
        }

        String name = data.get("name").toString();

        // 处理user_ids
        List<Integer> userIds = new ArrayList<>();
        if (data.containsKey("user_ids") && data.get("user_ids") != null) {
            Object userIdsObj = data.get("user_ids");
            if (userIdsObj instanceof List) {
                userIds = ((List<?>) userIdsObj).stream()
                        .map(obj -> Integer.parseInt(obj.toString()))
                        .collect(Collectors.toList());
            }
        }

        // 处理sort
        Integer sort = 0;
        if (data.containsKey("sort") && data.get("sort") != null) {
            sort = Integer.parseInt(data.get("sort").toString());
        }

        // PHP: if ($id) { $this->dao->update($id, $data); } else { $this->dao->save($data); }
        if (id != null && id > 0) {
            // 更新
            QrcodeEntity qrcode = qrcodeMapper.selectById(id);
            if (qrcode == null) {
                throw new CrmChatException("QR code does not exist");
            }
            TenantGuard.ensureOwnedByCurrentTenant(qrcode.getAppid(), "QR code does not exist");

            qrcode.setName(name);
            qrcode.setUserIds(userIds);
            qrcode.setSort(sort);

            int result = qrcodeMapper.updateById(qrcode);
            if (result <= 0) {
                throw new CrmChatException("Failed to modify");
            }
        } else {
            // 创建
            QrcodeEntity qrcode = new QrcodeEntity();
            qrcode.setName(name);
            qrcode.setUserIds(userIds);
            qrcode.setSort(sort);
            qrcode.setUrl(""); // PHP中默认为空字符串

            int result = qrcodeMapper.insert(qrcode);
            if (result <= 0) {
                throw new CrmChatException("Failed to save");
            }
        }
    }

    /**
     * 删除二维码
     * DELETE /api/tenant/chat/qrcode/:id
     *
     * PHP Reference: Qrcode.php::delete()
     *
     * @param id           二维码ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteQrcode(Integer id) {
        // PHP: if (!$id) return $this->fail('缺少参数');
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        QrcodeEntity qrcode = qrcodeMapper.selectById(id);
        if (qrcode == null) {
            throw new CrmChatException("QR code does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(qrcode.getAppid(), "QR code does not exist");

        // PHP: if ($this->services->delete($id)) {
        //     return $this->success('删除成功');
        // } else {
        //     return $this->success('删除失败');
        // }
        int result = qrcodeMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete");
        }
    }
}
