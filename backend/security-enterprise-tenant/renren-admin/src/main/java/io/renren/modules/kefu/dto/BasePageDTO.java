package io.renren.modules.kefu.dto;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class BasePageDTO {
    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer limit = 10;
}
