/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.renren.common.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel模板读取类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class ExcelDataListener<E, T> extends AnalysisEventListener<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelDataListener.class);
    /**
     * 每隔2000条存储数据库，然后清理list，方便内存回收
     */
    private static final int BATCH_COUNT = 2000;
    private final List<E> list = new ArrayList<>();

    /**
     * 通过构造器注入Service
     */
    private final BaseService<E> baseService;

    /**
     * 构造方法
     *
     * @param baseService Service对象
     */
    public ExcelDataListener(BaseService<E> baseService) {
        this.baseService = baseService;
    }

    /**
     * 每条数据解析完，都会调用此方法
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        LOGGER.info("解析到一条数据:{}", JsonUtils.toJsonString(data));

        E entity = ConvertUtils.sourceToTarget(data, baseService.currentModelClass());
        list.add(entity);

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }
    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        LOGGER.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        LOGGER.info("{}条数据，开始存储数据库！", list.size());
        baseService.insertBatch(list);
        LOGGER.info("存储数据库成功！");
    }
}