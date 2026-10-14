package com.zyr.shop.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页查询结果包装类
 *
 * @param <T> 列表元素类型
 * @author zyr
 */
@Data
public class PageResult<T> {

    /** 当前页码（从1开始） */
    private Long page;

    /** 每页条数 */
    private Long size;

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> records;

    /** 无参构造 */
    public PageResult() {
    }

    /**
     * 全参构造
     *
     * @param page    当前页码
     * @param size    每页条数
     * @param total   总记录数
     * @param records 当前页数据
     */
    public PageResult(Long page, Long size, Long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    /**
     * 基于 Spring Data 的 Page 对象快速构建分页结果
     *
     * @param pageData JPA 分页查询结果
     * @param <T>      元素类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(Page<T> pageData) {
        return new PageResult<>((long) pageData.getNumber() + 1,
                (long) pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getContent());
    }
}
