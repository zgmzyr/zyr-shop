package com.zyr.shop.config;

import com.zyr.shop.config.ZyrProperties.Alipay;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 支付宝密钥持有组件
 *
 * <p>应用启动时把 application-local.yml 中配置的私钥/公钥统一清洗为
 * 支付宝 SDK 要求的"纯 Base64 单行文本"，供：</p>
 * <ul>
 *     <li>{@link AlipayConfig} 创建 AlipayClient（下单签名用私钥）</li>
 *     <li>{@code AlipayService#handleNotify} 异步通知验签（用支付宝公钥）</li>
 * </ul>
 * 保证下单与验签使用的密钥形态完全一致，避免一处清洗、一处未清洗导致的验签失败。
 *
 * @author zyr
 */
@Component
@RequiredArgsConstructor
@Getter
public class AlipayKeys {

    /** 项目配置 */
    private final ZyrProperties properties;

    /** 清洗后的应用私钥（纯 Base64 单行） */
    private String privateKey = "";

    /** 清洗后的支付宝公钥（纯 Base64 单行） */
    private String publicKey = "";

    /**
     * 启动后初始化：清洗密钥
     */
    @PostConstruct
    public void init() {
        Alipay alipay = properties.getAlipay();
        this.privateKey = normalize(alipay.getAppPrivateKey());
        this.publicKey = normalize(alipay.getAlipayPublicKey());
    }

    /**
     * 判断支付配置是否完整（三项必填配置都已填写且不是占位提示文本）
     *
     * @return true 表示已完成沙箱凭据配置
     */
    public boolean isConfigured() {
        Alipay alipay = properties.getAlipay();
        return isRealValue(alipay.getAppId())
                && !privateKey.isEmpty()
                && !publicKey.isEmpty();
    }

    /**
     * 密钥标准化：去除 PEM 头尾、所有空白字符
     *
     * @param raw 原始密钥
     * @return 单行纯 Base64
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    /**
     * 判断配置值是否为真实值（排除空串与 application-local.yml 中的中文占位提示）
     *
     * @param value 配置值
     * @return true 表示是已填写的真实值
     */
    private boolean isRealValue(String value) {
        return value != null && !value.isBlank() && !value.startsWith("请替换");
    }
}
