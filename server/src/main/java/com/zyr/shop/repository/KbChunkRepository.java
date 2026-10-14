package com.zyr.shop.repository;

import com.zyr.shop.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 知识库分块数据访问接口
 *
 * @author zyr
 */
public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {

    /**
     * 按文档ID查询全部分块，按分块序号升序
     *
     * @param documentId 文档ID
     * @return 分块列表
     */
    List<KbChunk> findByDocumentIdOrderByChunkNoAsc(Long documentId);

    /**
     * 删除某文档的全部分块（删除知识文档时使用）
     *
     * @param documentId 文档ID
     */
    void deleteByDocumentId(Long documentId);
}
