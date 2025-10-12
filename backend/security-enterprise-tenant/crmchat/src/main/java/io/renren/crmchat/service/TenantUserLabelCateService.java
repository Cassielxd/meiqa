package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Tenant 用户标签分类管理服务
 * PHP Reference: /app/controller/tenant/user/LabelCate.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getCateList(): 获取分类列表
 *    - 根据type=0和appid查询
 * 2. createCate(): 创建分类
 *    - 验证name非空
 *    - 检查name唯一性（type=0）
 *    - 设置type=0、add_time=当前时间、appid
 *    - 保存后更新sort=id
 * 3. updateCate(): 更新分类
 *    - 验证name非空
 *    - 更新name和sort
 * 4. deleteCate(): 删除分类
 *    - 检查是否有标签关联
 *    - 如果有，返回错误"Please delete the labels under this category first"
 * 5. moveCate(): 排序移动
 *    - 批量更新排序（倒序）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantUserLabelCateService {

    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final ChatUserLabelMapper chatUserLabelMapper;

    /**
     * 获取分类列表
     * GET /api/tenant/user/label/cate
     *
     * PHP Reference: LabelCate.php::index()
     *
     * 业务逻辑:
     * 1. 根据type=0和appid查询分类列表
     *
     * @return 分类列表
     */
    public List<ChatUserLabelCateEntity> getCateList() {
        // PHP: $this->services->getCateList(['type' => 0, "appid" => $appid])

        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        wrapper.orderByAsc("id");
        return chatUserLabelCateMapper.selectList(wrapper);
    }

    /**
     * 创建分类
     * POST /api/tenant/user/label/cate
     *
     * PHP Reference: LabelCate.php::save()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 检查name唯一性（type=0）
     * 3. 设置type=0、add_time、appid
     * 4. 保存
     * 5. 更新sort=id
     *
     * @param data          分类数据（name, sort）
     * @return 新创建的分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createCate(Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        String name = data.get("name").toString();

        // 2. PHP: if ($this->services->count(['name' => $data['name'], 'type' => 0])) return $this->fail('分类名称相同');
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("type", 0);
        Long count = chatUserLabelCateMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Category name already exists");
        }

        // 3. PHP: $data['type'] = 0; $data['add_time'] = time(); $data["appid"] = $appid;
        ChatUserLabelCateEntity cate = new ChatUserLabelCateEntity();
        cate.setName(name);
        cate.setType(0);
        cate.setAddTime((int) (System.currentTimeMillis() / 1000));

        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            cate.setSort(0);
        }

        // 4. PHP: $res = $this->services->save($data);
        int result = chatUserLabelCateMapper.insert(cate);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
        }

        // 5. PHP: $this->services->update($res->id, ['sort' => $res->id]);
        cate.setSort(cate.getId());
        chatUserLabelCateMapper.updateById(cate);

        return cate.getId();
    }

    /**
     * 更新分类
     * PUT /api/tenant/user/label/cate/:id
     *
     * PHP Reference: LabelCate.php::update()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 验证分类存在
     * 3. 更新name和sort、设置type=0
     *
     * @param id           分类ID
     * @param data         分类数据（name, sort）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCate(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        String name = data.get("name").toString();

        // 2. 验证分类存在
        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }
        // Category是平台级配置，不需要租户检查

        // 3. PHP: $data['type'] = 0; $this->services->update($id, $data);
        cate.setName(name);
        cate.setType(0);

        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatUserLabelCateMapper.updateById(cate);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除分类
     * DELETE /api/tenant/user/label/cate/:id
     *
     * PHP Reference: LabelCate.php::delete()
     *
     * 业务逻辑:
     * 1. 验证分类存在
     * 2. 检查是否有标签关联此分类
     * 3. 如果有，返回错误
     * 4. 如果没有，删除分类
     *
     * @param id           分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCate(Integer id) {
        // 1. PHP: if (!$id) return $this->fail('缺少参数');
        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }
        // Category是平台级配置，不需要租户检查

        // 2. PHP: if ($services->count(['cate_id' => $id])) return $this->fail('请先删除分类下的标签');
        QueryWrapper<io.renren.crmchat.entity.ChatUserLabelEntity> labelWrapper = new QueryWrapper<>();
        labelWrapper.eq("cate_id", id);
        Long labelCount = chatUserLabelMapper.selectCount(labelWrapper);

        if (labelCount > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Please delete tags under this category first");
        }

        // 3. 删除分类
        int result = chatUserLabelCateMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 排序移动
     * POST /api/tenant/user/label/cate/move
     *
     * PHP Reference: LabelCate.php::move()
     *
     * 业务逻辑:
     * 1. 获取ids数组
     * 2. 倒序更新sort值（count+1, count, count-1...）
     *
     * @param ids          分类ID数组
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveCate(List<Integer> ids) {
        // PHP: labelMove($ids)
        // $sortMax = count($ids) + 1;
        // foreach ($ids as $id) { $this->dao->update($id, ['sort' => $sortMax]); $sortMax--; }

        int sortMax = ids.size() + 1;
        for (Integer id : ids) {
            ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
            if (cate == null) {
                continue;
            }
            // Category是平台级配置，不需要租户检查
            cate.setSort(sortMax);
            chatUserLabelCateMapper.updateById(cate);
            sortMax--;
        }
    }
}
