package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.AppVersionMapper;
import io.renren.crmchat.entity.AppVersionEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AppVersionService {

    private final AppVersionMapper appVersionMapper;

    public Map<String, Object> getList(Integer page, Integer limit) {
        QueryWrapper<AppVersionEntity> query = new QueryWrapper<>();
        query.isNull("delete_time");
        query.orderByDesc("id");

        Page<AppVersionEntity> pageObj = new Page<>(page, limit);
        IPage<AppVersionEntity> pageResult = appVersionMapper.selectPage(pageObj, query);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("count", (int) pageResult.getTotal());
        
        return result;
    }
}
