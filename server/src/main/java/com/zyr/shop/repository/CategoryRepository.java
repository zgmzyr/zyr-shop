package com.zyr.shop.repository;

import com.zyr.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 商品分类数据访问接口
 *
 * @author zyr
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 查询所有启用的分类并按 sort 升序排列
     *
     * @return 启用分类列表
     */
    List<Category> findByStatusOrderBySortAsc(Integer status);
}
