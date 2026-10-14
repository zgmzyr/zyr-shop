package com.zyr.shop.repository;

import com.zyr.shop.entity.KbDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库文档数据访问接口
 *
 * @author zyr
 */
public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {

    /**
     * 分页条件查询文档（支持按文件名模糊搜索、按文件类型过滤）
     *
     * @param keyword  文件名关键词
     * @param fileType 文件类型（可空）
     * @param pageable 分页参数
     * @return 文档分页结果
     */
    @Query("select d from KbDocument d where (:keyword is null or :keyword = '' " +
            "or d.fileName like concat('%', :keyword, '%')) " +
            "and (:fileType is null or :fileType = '' or d.fileType = :fileType) " +
            "order by d.id desc")
    Page<KbDocument> search(@Param("keyword") String keyword,
                            @Param("fileType") String fileType,
                            Pageable pageable);

    /**
     * 按文件类型统计文档数量（首页饼图）
     *
     * @return 每行：文件类型、数量
     */
    @Query("select d.fileType, count(d) from KbDocument d group by d.fileType")
    List<Object[]> countByFileType();

    /**
     * 统计指定时间之后创建的文档数量
     *
     * @param time 起始时间
     * @return 文档数量
     */
    long countByCreateTimeAfter(LocalDateTime time);

    /**
     * 按天统计指定时间范围内的文档上传数量（首页折线图）
     *
     * @param start 起始时间
     * @return 每行：日期字符串(yyyy-MM-dd)、数量
     */
    @Query(value = "select date_format(create_time, '%Y-%m-%d') as d, count(*) as c " +
            "from kb_document where create_time >= :start group by d order by d",
            nativeQuery = true)
    List<Object[]> countDailyAfter(@Param("start") LocalDateTime start);
}
