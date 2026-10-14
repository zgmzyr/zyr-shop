package com.zyr.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 项目自定义配置属性
 *
 * <p>对应 application.yml 中 zyr 前缀的全部配置项：JWT、上传目录、AI、支付宝沙箱</p>
 *
 * @author zyr
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zyr")
public class ZyrProperties {

    /** JWT 相关配置 */
    private Jwt jwt = new Jwt();

    /** 文件上传相关配置 */
    private Upload upload = new Upload();

    /** AI 大模型 / RAG 相关配置 */
    private Ai ai = new Ai();

    /** 支付宝沙箱支付配置 */
    private Alipay alipay = new Alipay();

    /**
     * JWT 配置
     */
    @Data
    public static class Jwt {
        /** 签名密钥 */
        private String secret;
        /** 令牌有效期（小时） */
        private Integer expireHours = 24;
    }

    /**
     * 上传目录配置
     */
    @Data
    public static class Upload {
        /** 文件统一保存根目录 */
        private String dir;
    }

    /**
     * AI 相关配置
     */
    @Data
    public static class Ai {
        /** 阿里云 DashScope 嵌入模型配置 */
        private Dashscope dashscope = new Dashscope();
        /** RAG 检索增强配置 */
        private Rag rag = new Rag();
    }

    /**
     * DashScope 嵌入模型配置（OpenAI 兼容模式）
     */
    @Data
    public static class Dashscope {
        /** 兼容接口基础地址 */
        private String baseUrl;
        /** API 密钥 */
        private String apiKey;
        /** 嵌入模型名称 */
        private String embeddingModel;
    }

    /**
     * RAG 检索参数配置
     */
    @Data
    public static class Rag {
        /** 检索返回的分块数量 */
        private Integer topK = 4;
        /** 相似度阈值 */
        private Double similarityThreshold = 0.5;
        /** 携带的历史消息条数 */
        private Integer chatHistoryLimit = 6;
    }

    /**
     * 支付宝沙箱配置
     */
    @Data
    public static class Alipay {
        /** 沙箱应用 APPID */
        private String appId;
        /** 商户应用私钥 */
        private String appPrivateKey;
        /** 支付宝公钥 */
        private String alipayPublicKey;
        /** 沙箱网关地址 */
        private String gateway;
        /** 异步通知回调地址 */
        private String notifyUrl;
        /** 同步返回页面地址 */
        private String returnUrl;
        /** 签名类型（RSA2） */
        private String signType = "RSA2";
        /** 字符编码 */
        private String charset = "UTF-8";
        /** 报文格式 */
        private String format = "json";
    }
}
