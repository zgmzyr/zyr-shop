package com.zyr.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库分块实体
 *
 * <p>记录每个知识文档切分后的分块内容与在 Redis 向量库中对应的向量ID，
 * 便于"查看分块"和"删除文档时同步删除向量数据"</p>
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "kb_chunk")
public class KbChunk {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属文档ID */
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /** 分块序号（从1开始） */
    @Column(name = "chunk_no", nullable = false)
    private Integer chunkNo;

    /** Redis 向量库中对应的向量ID（用于精准删除） */
    @Column(name = "vector_id", nullable = false, length = 64)
    private String vectorId;

    /** 分块文本内容 */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 插入前填充时间 */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
