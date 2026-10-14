package com.zyr.shop.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱支付配置类
 *
 * <p>使用支付宝官方 SDK（alipay-sdk-java），通过 {@link DefaultAlipayClient}
 * 构造一个全局唯一的 {@link AlipayClient}，后续支付下单、异步通知验签都复用它</p>
 *
 * <p><b>密钥格式说明（重要）：</b></p>
 * <ul>
 *     <li>支付宝 SDK 要求私钥/公钥为<b>纯 Base64 单行文本</b>，不能带 PEM 头尾与换行</li>
 *     <li>{@link AlipayKeys} 已统一自动清洗，application-local.yml
 *         中直接粘贴多行 PEM 或单行文本均可</li>
 *     <li>应用私钥必须是 <b>PKCS8</b> 格式（对应 -----BEGIN PRIVATE KEY-----），
 *         不能是 PKCS1（-----BEGIN RSA PRIVATE KEY-----）</li>
 * </ul>
 *
 * <p><b>沙箱凭据获取方式：</b></p>
 * <ol>
 *     <li>登录 <a href="https://open.alipay.com/develop/sandbox/app">支付宝开放平台沙箱</a></li>
 *     <li>获取沙箱 APPID；接口加签方式选择"公钥模式"</li>
 *     <li>用支付宝开放平台密钥工具生成 RSA2(PKCS8,2048) 密钥对，应用公钥上传沙箱页</li>
 *     <li>把应用私钥、沙箱页返回的支付宝公钥填入 application-local.yml</li>
 *     <li>沙箱买家账号在沙箱页"沙箱账号"处获取，用于登录沙箱页面付款</li>
 * </ol>
 *
 * @author zyr
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlipayConfig {

    /** 统一清洗后的密钥 */
    private final AlipayKeys alipayKeys;

    /**
     * 构造支付宝客户端
     *
     * <p>AlipayClient 是线程安全的，全局单例复用即可。
     * 密钥未配置时客户端仍会创建（不影响其它功能启动），调用支付时会给出明确报错</p>
     *
     * @param properties 项目自定义配置（zyr.alipay）
     * @return 支付宝客户端实例
     */
    @Bean
    public AlipayClient alipayClient(ZyrProperties properties) {
        ZyrProperties.Alipay alipay = properties.getAlipay();

        log.info("【支付配置】初始化支付宝沙箱客户端：appId={}, gateway={}",
                mask(alipay.getAppId()), alipay.getGateway());
        log.info("【支付配置】应用私钥长度={}, 支付宝公钥长度={}（长度为 0 表示未配置，支付功能不可用）",
                alipayKeys.getPrivateKey().length(), alipayKeys.getPublicKey().length());

        // DefaultAlipayClient 参数顺序：网关地址、APPID、应用私钥、报文格式、字符编码、支付宝公钥、签名类型
        return new DefaultAlipayClient(
                alipay.getGateway(),
                alipay.getAppId(),
                alipayKeys.getPrivateKey(),
                alipay.getFormat(),
                alipay.getCharset(),
                alipayKeys.getPublicKey(),
                alipay.getSignType());
    }

    /**
     * 敏感信息脱敏（保留首尾少量字符用于辨认，中间用 * 代替）
     *
     * @param value 原始字符串
     * @return 脱敏后的字符串
     */
    private String mask(String value) {
        if (value == null || value.isEmpty()) {
            return "(空)";
        }
        if (value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }
}
