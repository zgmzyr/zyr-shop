package com.zyr.shop.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zyr.shop.common.BusinessException;
import com.zyr.shop.config.AlipayKeys;
import com.zyr.shop.config.ZyrProperties;
import com.zyr.shop.entity.Payment;
import com.zyr.shop.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付服务
 *
 * <p>基于支付宝电脑网站支付能力（alipay.trade.page.pay）实现：</p>
 * <ol>
 *     <li>下单后调用 {@link #createPagePay} 生成支付页表单 HTML，前端直接渲染跳转</li>
 *     <li>用户在支付宝完成付款后，支付宝异步回调 {@code notifyUrl}，由 {@link #handleNotify} 验签并更新订单状态</li>
 *     <li>同步返回仅用于展示，不作为支付完成的依据</li>
 * </ol>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    /** 支付宝客户端（AlipayConfig 中注入） */
    private final AlipayClient alipayClient;

    /** 项目配置（用于读取 notifyUrl / returnUrl） */
    private final ZyrProperties properties;

    /** 清洗后的支付宝密钥（下单与验签共用同一份，保证形态一致） */
    private final AlipayKeys alipayKeys;

    /** 订单服务（用于标记已支付） */
    private final OrderService orderService;

    /** 支付记录数据访问 */
    private final PaymentRepository paymentRepository;

    /**
     * 创建电脑网站支付订单，返回支付页表单 HTML
     *
     * <p>商户订单号 = 业务订单号；金额取自订单；subject 取商品名拼接</p>
     *
     * @param orderNo    订单号
     * @param totalAmount 订单总金额
     * @param subject    订单标题
     * @param userId     下单用户ID
     * @return 支付页表单 HTML（含自动提交脚本），前端直接 innerHTML 即可跳转到支付宝
     */
    public String createPagePay(String orderNo, BigDecimal totalAmount, String subject, Long userId) {
        // 0. 配置完整性校验：未填写沙箱凭据时给出明确可读的错误，而不是抛出底层签名异常
        if (!alipayKeys.isConfigured()) {
            log.warn("【支付】支付宝沙箱未配置完整（app-id / 应用私钥 / 支付宝公钥），请填写 application-local.yml");
            throw new BusinessException(500,
                    "支付宝沙箱尚未配置：请在 server/src/main/resources/application-local.yml 中填写 app-id、应用私钥、支付宝公钥后重启后端");
        }
        // 1. 落库一条 PENDING 的支付记录（便于统计与排查）
        Payment payment = paymentRepository.findByOrderNo(orderNo).orElseGet(() -> {
            Payment p = new Payment();
            p.setOrderNo(orderNo);
            p.setUserId(userId);
            p.setAmount(totalAmount);
            p.setPayType("ALIPAY");
            p.setStatus("PENDING");
            return p;
        });
        payment.setAmount(totalAmount);
        payment.setUserId(userId);
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        // 2. 构造 SDK 请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        ZyrProperties.Alipay alipay = properties.getAlipay();
        request.setNotifyUrl(alipay.getNotifyUrl());
        request.setReturnUrl(alipay.getReturnUrl());
        // bizContent：支付宝电脑网站支付必填参数
        String bizContent = "{" +
                "\"out_trade_no\":\"" + orderNo + "\"," +
                "\"total_amount\":\"" + totalAmount.toPlainString() + "\"," +
                "\"subject\":\"" + (subject == null ? "zyr_shop商城订单" : subject) + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}";
        request.setBizContent(bizContent);
        // 3. 调 SDK 生成表单 HTML
        try {
            String form = alipayClient.pageExecute(request).getBody();
            log.info("【支付】生成支付表单 orderNo={} amount={}", orderNo, totalAmount);
            return form;
        } catch (Exception e) {
            log.error("【支付】生成支付表单失败 orderNo={}", orderNo, e);
            throw new BusinessException(500, "支付下单失败：" + e.getMessage());
        }
    }

    /**
     * 处理支付宝异步通知
     *
     * <p>步骤：</p>
     * <ol>
     *     <li>验签（用支付宝公钥，RSA2）</li>
     *     <li>校验 trade_status == TRADE_SUCCESS 或 TRADE_FINISHED</li>
     *     <li>更新 payment 表（交易号、买家ID、状态、原始报文）</li>
     *     <li>调用 {@link OrderService#markPaid} 把订单标记为 PAID</li>
     *     <li>返回字符串 "success"（支付宝要求，否则会重复通知）</li>
     * </ol>
     *
     * @param request HTTP 请求（包含支付宝 POST 来的全部参数）
     * @return "success" 表示处理成功
     */
    @Transactional
    public String handleNotify(HttpServletRequest request) {
        // 1. 收集支付宝请求参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        log.info("【支付】收到支付宝异步通知：{}", params);

        // 2. 验签
        try {
            boolean signOk = AlipaySignature.rsaCheckV1(
                    params,
                    alipayKeys.getPublicKey(),
                    properties.getAlipay().getCharset(),
                    properties.getAlipay().getSignType());
            if (!signOk) {
                log.warn("【支付】支付宝异步通知验签失败");
                return "fail";
            }
        } catch (Exception e) {
            log.error("【支付】支付宝异步通知验签异常", e);
            return "fail";
        }

        // 3. 校验交易状态
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("【支付】交易状态 {}，忽略", tradeStatus);
            return "success";
        }

        // 4. 更新本地支付记录 + 订单状态
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String buyerId = params.get("buyer_id");
        String gmtPayment = params.get("gmt_payment");
        Payment payment = paymentRepository.findByOrderNo(orderNo).orElse(null);
        if (payment != null) {
            payment.setTradeNo(tradeNo);
            payment.setBuyerId(buyerId);
            payment.setStatus("SUCCESS");
            if (gmtPayment != null) {
                try {
                    payment.setPaidAt(LocalDateTime.parse(gmtPayment,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    payment.setPaidAt(LocalDateTime.now());
                }
            } else {
                payment.setPaidAt(LocalDateTime.now());
            }
            payment.setRawNotify(params.toString());
            paymentRepository.save(payment);
        }
        // 5. 标记订单已支付
        try {
            orderService.markPaid(orderNo);
        } catch (Exception e) {
            log.error("【支付】订单状态更新失败 orderNo={}", orderNo, e);
        }
        return "success";
    }
}
