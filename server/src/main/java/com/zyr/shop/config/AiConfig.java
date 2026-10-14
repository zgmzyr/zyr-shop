package com.zyr.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置类
 *
 * <p>本系统组合使用两家模型服务：</p>
 * <ol>
 *     <li>对话大模型：DeepSeek 的 deepseek-chat（由 starter 自动装配 DeepSeekChatModel）</li>
 *     <li>嵌入模型：阿里云百炼 DashScope 的 text-embedding-v3（OpenAI 兼容接口，此处手动声明 Bean）</li>
 * </ol>
 * <p>Redis 向量库（VectorStore）由 spring-ai-starter-vector-store-redis 自动装配，
 * 它会自动使用本类声明的 EmbeddingModel 完成文本向量化</p>
 *
 * @author zyr
 */
@Slf4j
@Configuration
public class AiConfig {

    /**
     * 声明嵌入模型 Bean
     *
     * <p>DashScope 提供 OpenAI 兼容接口，因此直接复用 Spring AI 的 OpenAiEmbeddingModel，
     * 只需把 baseUrl 指向 DashScope 的兼容地址即可</p>
     *
     * @param properties 项目自定义配置
     * @return 嵌入模型实例
     */
    @Bean
    public EmbeddingModel embeddingModel(ZyrProperties properties) {
        ZyrProperties.Dashscope dashscope = properties.getAi().getDashscope();
        log.info("【AI配置】初始化 DashScope 嵌入模型：baseUrl={}, model={}",
                dashscope.getBaseUrl(), dashscope.getEmbeddingModel());
        return new OpenAiEmbeddingModel(
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .baseUrl(dashscope.getBaseUrl())
                        .apiKey(dashscope.getApiKey())
                        .model(dashscope.getEmbeddingModel())
                        .build());
    }

    /**
     * 声明 ChatClient（Spring AI 2.0 推荐的高层对话 API）
     *
     * @param chatModel starter 自动装配好的 DeepSeek 对话模型
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(DeepSeekChatModel chatModel) {
        log.info("【AI配置】初始化 DeepSeek ChatClient（model=deepseek-chat）");
        return ChatClient.builder(chatModel).build();
    }
}
