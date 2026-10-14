package com.zyr.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体
 *
 * <p>记录智能客服知识库上传文件的基本信息与向量化处理状态</p>
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "kb_document")
public class KbDocument {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 原始文件名 */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** 服务器磁盘绝对路径 */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** HTTP 访问相对路径 */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /** 文件类型：TXT / DOC / PDF / MD */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /** 文件大小（字节） */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** 切分后的知识分块数量 */
    @Column(name = "chunk_count", nullable = false)
    private Integer chunkCount;

    /** 处理状态：PROCESSING-处理中，READY-已就绪，FAILED-失败 */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** 上传人用户ID */
    @Column(name = "uploader_id")
    private Long uploaderId;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /** 插入前初始化默认值与时间戳 */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = "PROCESSING";
        }
        if (this.chunkCount == null) {
            this.chunkCount = 0;
        }
        if (this.fileSize == null) {
            this.fileSize = 0L;
        }
    }

    /** 更新前刷新更新时间 */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
