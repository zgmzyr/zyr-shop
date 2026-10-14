package com.zyr.shop.service;

import com.zyr.shop.repository.ChatMessageRepository;
import com.zyr.shop.repository.KbChunkRepository;
import com.zyr.shop.repository.KbDocumentRepository;
import com.zyr.shop.repository.OrderRepository;
import com.zyr.shop.repository.ProductRepository;
import com.zyr.shop.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台数据统计服务
 *
 * <p>为管理员首页数据看板提供：核心指标、近7天订单趋势、销售额趋势、
 * 商品分类分布、用户角色分布、客服问答趋势</p>
 *
 * @author zyr
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    /** 短日期格式化（图表 X 轴显示） */
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    /** 用户数据访问 */
    private final SysUserRepository userRepository;

    /** 订单数据访问 */
    private final OrderRepository orderRepository;

    /** 商品数据访问 */
    private final ProductRepository productRepository;

    /** 知识库文档数据访问 */
    private final KbDocumentRepository documentRepository;

    /** 知识库分块数据访问 */
    private final KbChunkRepository chunkRepository;

    /** 客服消息数据访问 */
    private final ChatMessageRepository messageRepository;

    /**
     * 核心指标卡片数据
     *
     * @return 指标 Map
     */
    public Map<String, Object> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCount", userRepository.count());
        result.put("productCount", productRepository.count());
        result.put("orderCount", orderRepository.count());
        // 累计销售额（已支付订单）
        result.put("totalSales", orderRepository.sumTotalSales() == null ? 0.0 : orderRepository.sumTotalSales());
        // 今日新增订单数
        result.put("todayOrderCount", orderRepository.countByCreateTimeAfter(todayStart));
        // 客服问答累计轮数
        result.put("qaCount", messageRepository.countByRole("assistant"));
        result.put("todayQaCount", messageRepository.countByRoleAndCreateTimeAfter("assistant", todayStart));
        return result;
    }

    /**
     * 近 N 天订单趋势（折线图）
     *
     * @param days 天数
     * @return 日期-数量列表
     */
    public List<Map<String, Object>> orderTrend(int days) {
        LocalDate start = LocalDate.now().minusDays((long) days - 1);
        Map<String, Long> counted = toDateMap(orderRepository.countDailyAfter(start.atStartOfDay()));
        return fillMissingDays(start, days, counted);
    }

    /**
     * 近 N 天销售额趋势（柱状图，仅统计已支付订单）
     *
     * @param days 天数
     * @return 日期-销售额列表
     */
    public List<Map<String, Object>> salesTrend(int days) {
        LocalDate start = LocalDate.now().minusDays((long) days - 1);
        // 行集合：[日期, 销售额]
        Map<String, Double> counted = new LinkedHashMap<>();
        for (Object[] row : orderRepository.sumDailySalesAfter(start.atStartOfDay())) {
            counted.put(String.valueOf(row[0]), ((Number) row[1]).doubleValue());
        }
        // 补全缺失日期
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            double value = counted.getOrDefault(day.format(keyFormatter), 0.0);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", day.format(DAY_FORMATTER));
            entry.put("value", value);
            result.add(entry);
        }
        return result;
    }

    /**
     * 商品分类分布（饼图，基于商品数量）
     *
     * <p>由于产品 Repository 仅返回 categoryId 与 count，这里不主动 join 名称，
     * 由调用方/前端用分类 ID 标注即可；如需分类名请前端单独请求 /api/product/categories</p>
     *
     * @return categoryId-value 列表
     */
    public List<Map<String, Object>> productCategoryDistribution() {
        Map<Long, Long> raw = new LinkedHashMap<>();
        for (Object[] row : productRepository.countByCategory()) {
            raw.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        raw.forEach((categoryId, count) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("categoryId", categoryId);
            entry.put("value", count);
            result.add(entry);
        });
        return result;
    }

    /**
     * 用户角色分布（环形图）
     *
     * @return 名称-数量列表
     */
    public List<Map<String, Object>> userRoleDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> admin = new LinkedHashMap<>();
        admin.put("name", "管理员");
        admin.put("value", userRepository.countByRole("ADMIN"));
        list.add(admin);
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("name", "普通用户");
        user.put("value", userRepository.countByRole("USER"));
        list.add(user);
        return list;
    }

    /**
     * 客服问答趋势（柱状图，按用户提问消息统计）
     *
     * @param days 天数
     * @return 日期-数量列表
     */
    public List<Map<String, Object>> chatTrend(int days) {
        LocalDate start = LocalDate.now().minusDays((long) days - 1);
        Map<String, Long> counted = toDateMap(messageRepository.countUserMessagesDailyAfter(start.atStartOfDay()));
        return fillMissingDays(start, days, counted);
    }

    /**
     * 知识库文档类型分布（饼图）
     *
     * @return 名称-数量列表
     */
    public List<Map<String, Object>> documentTypeDistribution() {
        Map<String, Long> typeCount = new LinkedHashMap<>();
        typeCount.put("TXT", 0L);
        typeCount.put("DOC", 0L);
        typeCount.put("PDF", 0L);
        typeCount.put("MD", 0L);
        for (Object[] row : documentRepository.countByFileType()) {
            typeCount.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        List<Map<String, Object>> list = new ArrayList<>();
        typeCount.forEach((type, count) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", type);
            entry.put("value", count);
            list.add(entry);
        });
        return list;
    }

    /**
     * 将 Repository 返回的 [日期, 数量] 行集合转换为 Map
     *
     * @param rows 数据行
     * @return 日期(yyyy-MM-dd) -> 数量
     */
    private Map<String, Long> toDateMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * 补齐缺失日期（没有数据的日期补 0），保证图表 X 轴连续
     *
     * @param start   起始日期
     * @param days    天数
     * @param counted 已统计出的数据
     * @return 连续的日期-数量列表
     */
    private List<Map<String, Object>> fillMissingDays(LocalDate start, int days, Map<String, Long> counted) {
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            long value = counted.getOrDefault(day.format(keyFormatter), 0L);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", day.format(DAY_FORMATTER));
            entry.put("value", value);
            result.add(entry);
        }
        return result;
    }
}
