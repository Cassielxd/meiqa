package io.renren.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("ai_model")
@EqualsAndHashCode(callSuper = true)
public class AiModelEntity extends BaseEntity {
    /**
     * 所属平台
     */
    private String platform;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型标识
     */
    private String model;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * API秘钥
     */
    private String apiKey;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;

}