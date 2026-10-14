package com.zyr.shop.repository;

import com.zyr.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品数据访问接口
 *
 * @author zyr
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 前台商品分页查询（仅上架商品，支持按分类、关键词过滤）
     *
     * @param categoryId 分类ID（可空）
     * @param keyword    关键词（可空）
     * @param pageable   分页参数
     * @return 商品分页结果
     */
    @Query("select p from Product p where p.status = 1 " +
            "and (:categoryId is null or p.categoryId = :categoryId) " +
            "and (:keyword is null or :keyword = '' or p.name like concat('%', :keyword, '%') or p.description like concat('%', :keyword, '%')) " +
            "order by p.sales desc, p.id desc")
    Page<Product> searchOnSale(@Param("categoryId") Long categoryId,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    /**
     * 管理员商品分页查询（不限状态，支持按关键词过滤）
     *
     * @param keyword  关键词（可空）
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    @Query("select p from Product p where (:keyword is null or :keyword = '' " +
            "or p.name like concat('%', :keyword, '%')) order by p.id desc")
    Page<Product> searchAdmin(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 扣减库存并增加销量（下单使用，乐观地按ID更新）
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 受影响行数（0 表示库存不足）
     */
    @Modifying
    @Query("update Product p set p.stock = p.stock - :quantity, p.sales = p.sales + :quantity where p.id = :productId and p.stock >= :quantity")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 统计指定时间之后的上架商品数（首页指标用）
     *
     * @param time 起始时间
     * @return 数量
     */
    long countByCreateTimeAfter(LocalDateTime time);

    /**
     * 按分类ID统计商品数（首页分类饼图用）
     *
     * @return 每行：分类ID、数量
     */
    @Query("select p.categoryId, count(p) from Product p group by p.categoryId")
    List<Object[]> countByCategory();
}
