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
 * 客服聊天消息实体
 *
 * <p>记录智能客服对话的用户提问与 AI 回答，用于在 Redis 向量库检索后回填历史上下文</p>
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID（前台用户或管理员） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 消息角色：user-用户提问，assistant-AI回答 */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    /** 消息内容 */
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
