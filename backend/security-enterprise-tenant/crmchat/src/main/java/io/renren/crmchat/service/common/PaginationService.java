package io.renren.crmchat.service.common;

import io.renren.common.page.PageData;
import io.renren.crmchat.exception.CrmChatException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 统一分页服务
 * 提供分页参数解析和 PageData 构建的公共逻辑
 *
 * 设计原则:
 * - Single Responsibility: 专注于分页逻辑
 * - DRY: 消除各 Service 中重复的分页代码
 * - Consistent: 统一全系统的分页行为
 *
 * PHP 参考:
 * - crmeb/basic/BaseServices.php::getPageValue()
 * - ThinkPHP 分页参数：page, limit
 *
 * @author CRMChat Team
 */
@Service
public class PaginationService {

    /**
     * 默认页码（第一页）
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页记录数
     */
    public static final int DEFAULT_LIMIT = 10;

    /**
     * 最大每页记录数（防止内存溢出）
     */
    public static final int MAX_LIMIT = 1000;

    /**
     * 从请求参数中提取分页参数
     *
     * PHP 对比:
     * - PHP: $this->getPageValue() 返回 [$page, $limit]
     * - Java: 返回 PaginationParams 对象
     *
     * @param params 请求参数 Map（通常来自 Controller）
     * @return PaginationParams 包含 page, limit, offset
     */
    public PaginationParams extractParams(Map<String, Object> params) {
        if (params == null) {
            return new PaginationParams(DEFAULT_PAGE, DEFAULT_LIMIT);
        }

        // 提取 page 参数
        int page = parseIntParam(params.get("page"), DEFAULT_PAGE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }

        // 提取 limit 参数
        int limit = parseIntParam(params.get("limit"), DEFAULT_LIMIT);
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        // 限制最大值（防止恶意请求）
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        return new PaginationParams(page, limit);
    }

    /**
     * 创建 PageData 对象
     *
     * PHP 对比:
     * - PHP: return compact('list', 'count');
     * - Java: return new PageData<>(list, total);
     *
     * @param list  数据列表
     * @param total 总记录数
     * @param <T>   数据类型
     * @return PageData 对象
     */
    public <T> PageData<T> createPageData(List<T> list, long total) {
        return new PageData<>(list, total);
    }

    /**
     * 计算 offset（用于 MyBatis 查询）
     *
     * SQL 分页:
     * - SELECT * FROM table LIMIT #{offset}, #{limit}
     * - offset = (page - 1) * limit
     *
     * @param page  页码（从1开始）
     * @param limit 每页记录数
     * @return offset 偏移量
     */
    public int calculateOffset(int page, int limit) {
        if (page < 1) {
            page = 1;
        }
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        return (page - 1) * limit;
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param limit 每页记录数
     * @return 总页数
     */
    public int calculateTotalPages(long total, int limit) {
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        return (int) Math.ceil((double) total / limit);
    }

    /**
     * 验证页码合法性
     *
     * @param page       当前页码
     * @param totalPages 总页数
     * @throws CrmChatException 页码超出范围时抛出
     */
    public void validatePage(int page, int totalPages) {
        if (page < 1) {
            throw new CrmChatException("Page number cannot be less than 1");
        }
        if (totalPages > 0 && page > totalPages) {
            throw new CrmChatException(
                    String.format("Page number out of range, total pages: %d", totalPages)
            );
        }
    }

    /**
     * 解析参数为整数
     *
     * @param value        参数值（可能是 String, Integer, Long 等）
     * @param defaultValue 默认值
     * @return 解析后的整数值
     */
    private int parseIntParam(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (NumberFormatException e) {
            // 解析失败，返回默认值
        }

        return defaultValue;
    }

    /**
     * 分页参数封装类
     *
     * 包含分页所需的所有参数：
     * - page: 页码（从1开始）
     * - limit: 每页记录数
     * - offset: 数据库查询偏移量
     */
    public static class PaginationParams {
        private final int page;
        private final int limit;
        private final int offset;

        public PaginationParams(int page, int limit) {
            this.page = page;
            this.limit = limit;
            this.offset = (page - 1) * limit;
        }

        public int getPage() {
            return page;
        }

        public int getLimit() {
            return limit;
        }

        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return String.format("PaginationParams{page=%d, limit=%d, offset=%d}",
                    page, limit, offset);
        }
    }
}
