package io.renren.crmchat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.QrcodeEntity;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.mapper.QrcodeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端 - 客服二维码管理
 * 参考PHP: app/controller/admin/chat/Qrcode.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat/qrcode")
@AllArgsConstructor
@Slf4j
public class AdminQrcodeController {

    private final QrcodeMapper qrcodeMapper;
    private final ChatServiceMapper chatServiceMapper;

    /**
     * 获取二维码列表
     * 参考PHP: Qrcode::index() -> QrcodeServices::getList()
     *
     * @param name 二维码名称（可选，用于搜索）
     * @param page 页码
     * @param limit 每页数量
     * @return 二维码列表
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "15") Integer limit) {

        log.info("获取二维码列表 - name: {}, page: {}, limit: {}", name, page, limit);

        // 构建查询条件
        QueryWrapper<QrcodeEntity> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("sort", "id");

        // 分页查询
        int offset = (page - 1) * limit;

        // 获取列表
        List<QrcodeEntity> list = qrcodeMapper.selectList(
            queryWrapper.last("LIMIT " + offset + ", " + limit)
        );

        // 收集所有用户IDs
        Set<Integer> userIds = new HashSet<>();
        for (QrcodeEntity item : list) {
            if (item.getUserIds() != null) {
                userIds.addAll(item.getUserIds());
            }
        }

        // 查询客服账号信息
        Map<Integer, String> kefuAccountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<ChatServiceEntity> serviceQuery = new QueryWrapper<>();
            serviceQuery.in("id", userIds);
            List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(serviceQuery);

            for (ChatServiceEntity kefu : kefuList) {
                kefuAccountMap.put(kefu.getId(), kefu.getAccount());
            }
        }

        // 填充客服账号列表
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

        // 获取总数
        Long count = qrcodeMapper.selectCount(queryWrapper);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);

        log.info("查询到 {} 条二维码记录", list.size());

        return new Result<Map<String, Object>>().ok(result);
    }

    /**
     * 创建二维码
     * 参考PHP: Qrcode::save()
     */
    @PostMapping
    public Result<String> save(@RequestBody QrcodeEntity qrcode) {
        log.info("创建二维码 - name: {}", qrcode.getName());
        qrcodeMapper.insert(qrcode);
        return new Result<String>().ok("Saved successfully");
    }

    /**
     * 更新二维码
     * 参考PHP: Qrcode::save($id)
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody QrcodeEntity qrcode) {
        log.info("更新二维码 - id: {}, name: {}", id, qrcode.getName());
        qrcode.setId(id);
        qrcodeMapper.updateById(qrcode);
        return new Result<String>().ok("Modified successfully");
    }

    /**
     * 删除二维码
     * 参考PHP: Qrcode::delete($id)
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除二维码 - id: {}", id);
        if (id == null) {
            return new Result<String>().error("Missing required parameter");
        }
        int result = qrcodeMapper.deleteById(id);
        if (result > 0) {
            return new Result<String>().ok("Deleted successfully");
        } else {
            return new Result<String>().error("Failed to delete");
        }
    }
}
