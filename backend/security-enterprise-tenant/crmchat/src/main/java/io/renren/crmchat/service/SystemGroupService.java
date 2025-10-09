package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.SystemGroupMapper;
import io.renren.crmchat.entity.SystemGroupEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组合数据服务
 * PHP Reference: app/services/system/config/SystemGroupServices.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class SystemGroupService {

    private final SystemGroupMapper systemGroupMapper;

    /**
     * 获取组合数据列表
     * PHP Reference: SystemGroupServices::getGroupList()
     *
     * @param title 搜索关键字
     * @param page 页码
     * @param limit 每页数量
     * @return 列表数据
     */
    public Map<String, Object> getGroupList(String title, Integer page, Integer limit) {
        // 构建查询条件
        QueryWrapper<SystemGroupEntity> query = new QueryWrapper<>();

        // PHP搜索器: whereLIke('id|name|info|config_name', "%$value%")
        if (title != null && !title.isEmpty()) {
            query.and(wrapper -> wrapper
                .like("id", title)
                .or().like("name", title)
                .or().like("info", title)
                .or().like("config_name", title)
            );
        }

        // 分页查询
        Page<SystemGroupEntity> pageObj = new Page<>(page, limit);
        IPage<SystemGroupEntity> pageResult = systemGroupMapper.selectPage(pageObj, query);

        List<Map<String, Object>> list = pageResult.getRecords().stream()
            .map(entity -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", entity.getId());
                map.put("cate_id", entity.getCateId());
                map.put("name", entity.getName());
                map.put("info", entity.getInfo());
                map.put("config_name", entity.getConfigName());
                // PHP: 如果有fields字段,转为typelist
                if (entity.getFields() != null) {
                    map.put("typelist", entity.getFields());
                }
                return map;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", pageResult.getTotal());
        return result;
    }

    /**
     * 获取所有组合数据(简化版)
     * PHP Reference: Group::getGroup()
     * 返回格式: [{"id": 1, "name": "组名"}]
     *
     * @return 组合数据列表
     */
    public List<Map<String, Object>> getGroupAll() {
        QueryWrapper<SystemGroupEntity> query = new QueryWrapper<>();
        query.eq("cate_id", 1);
        query.select("id", "name");

        List<SystemGroupEntity> entities = systemGroupMapper.selectList(query);

        return entities.stream()
            .map(entity -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", entity.getId());
                map.put("name", entity.getName());
                return map;
            })
            .collect(Collectors.toList());
    }
}
