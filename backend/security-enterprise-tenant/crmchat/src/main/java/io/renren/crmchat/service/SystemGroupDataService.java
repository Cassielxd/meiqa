package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.crmchat.dao.SystemGroupDataMapper;
import io.renren.crmchat.dao.SystemGroupMapper;
import io.renren.crmchat.entity.SystemGroupDataEntity;
import io.renren.crmchat.entity.SystemGroupEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 组合数据详情服务
 * PHP Reference: SystemGroupDataServices
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class SystemGroupDataService {

    private final SystemGroupDataMapper systemGroupDataMapper;
    private final SystemGroupMapper systemGroupMapper;
    private final ObjectMapper objectMapper;

    /**
     * 根据configName获取组合数据列表
     * PHP Reference: getGroupDataList() + getConfigNameValue()
     *
     * @param configName 配置名称,如: kf_adv, privacy, kf_icon
     * @param status 状态筛选 (可选)
     * @param page 页码
     * @param limit 每页数量
     * @return {list: [], count: N}
     */
    public Map<String, Object> getGroupDataByConfigName(String configName, Integer status,
                                                         Integer page, Integer limit) {
        // 1. 根据configName查询gid
        // PHP: $systemGroupServices->getConfigNameId($configName)
        QueryWrapper<SystemGroupEntity> groupQuery = new QueryWrapper<>();
        groupQuery.eq("config_name", configName);
        SystemGroupEntity group = systemGroupMapper.selectOne(groupQuery);

        if (group == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("list", Collections.emptyList());
            empty.put("count", 0);
            return empty;
        }

        Integer gid = group.getId();

        // 2. 根据gid查询数据
        QueryWrapper<SystemGroupDataEntity> dataQuery = new QueryWrapper<>();
        dataQuery.eq("gid", gid);

        if (status != null) {
            dataQuery.eq("status", status);
        }

        dataQuery.orderByAsc("sort");
        dataQuery.orderByDesc("id");

        Page<SystemGroupDataEntity> pageObj = new Page<>(page, limit);
        IPage<SystemGroupDataEntity> pageResult = systemGroupDataMapper.selectPage(pageObj, dataQuery);

        List<SystemGroupDataEntity> entities = pageResult.getRecords();
        long totalCount = pageResult.getTotal();

        // 3. 解析value字段JSON,转换为前端需要的格式
        // PHP: foreach ($value as $key => $item) { ... json_decode($item["value"]) ... }
        List<Map<String, Object>> list = new ArrayList<>();
        for (SystemGroupDataEntity entity : entities) {
            try {
                Map<String, Object> item = new HashMap<>();
                item.put("id", entity.getId());
                item.put("status", entity.getStatus());
                item.put("sort", entity.getSort());

                // Parse JSON value field
                String valueJson = entity.getValue();
                if (valueJson != null && !valueJson.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = objectMapper.readValue(valueJson, Map.class);

                    // PHP: foreach ($fields as $index => $field) { $data[$key][$index] = $field["value"]; }
                    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fieldData = (Map<String, Object>) entry.getValue();
                        Object fieldValue = fieldData.get("value");

                        // 处理图片路径 (PHP: set_file_url)
                        String fieldType = (String) fieldData.get("type");
                        if ("upload".equals(fieldType) || "uploads".equals(fieldType)) {
                            // TODO: 如果需要处理文件URL前缀,在这里添加逻辑
                            // fieldValue = addFileUrlPrefix(fieldValue);
                        }

                        item.put(entry.getKey(), fieldValue);
                    }
                }

                list.add(item);
            } catch (Exception e) {
                log.error("解析组合数据JSON失败: id={}, value={}", entity.getId(), entity.getValue(), e);
                // 出错时保留原始数据
                Map<String, Object> item = new HashMap<>();
                item.put("id", entity.getId());
                item.put("status", entity.getStatus());
                item.put("value", entity.getValue());
                list.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", totalCount);

        return result;
    }
}
