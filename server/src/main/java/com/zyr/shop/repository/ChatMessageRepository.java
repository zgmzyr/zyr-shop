package com.zyr.shop.repository;

import com.zyr.shop.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服聊天消息数据访问接口
 *
 * @author zyr
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 查询某用户最近 N 条消息，按时间倒序（取历史对话上下文时使用）
     *
     * @param userId   用户ID
     * @param pageable 分页参数（用于限制条数）
     * @return 消息列表（倒序）
     */
    List<ChatMessage> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    /**
     * 统计某角色的消息数（首页指标：assistant 消息数即累计问答轮数）
     *
     * @param role 消息角色
     * @return 数量
     */
    long countByRole(String role);

    /**
     * 统计指定时间后某角色的消息数
     *
     * @param role 消息角色
     * @param time 起始时间
     * @return 数量
     */
    long countByRoleAndCreateTimeAfter(String role, LocalDateTime time);

    /**
     * 按天统计用户提问数（首页柱状图）
     *
     * @param start 起始时间
     * @return 每行：日期字符串(yyyy-MM-dd)、数量
     */
    @Query(value = "select date_format(create_time, '%Y-%m-%d') as d, count(*) as c " +
            "from chat_message where role = 'user' and create_time >= :start group by d order by d",
            nativeQuery = true)
    List<Object[]> countUserMessagesDailyAfter(@Param("start") LocalDateTime start);
}
