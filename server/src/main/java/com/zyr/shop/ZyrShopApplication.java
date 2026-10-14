package com.zyr.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * zyr_shop 商城系统启动入口
 *
 * <p>本项目是一个基于 Spring AI 2.0 的商城系统，特性包括：</p>
 * <ul>
 *     <li>双角色：管理员（Vue3 后台）/ 普通用户（Thymeleaf 前台）</li>
 *     <li>商品/分类/购物车/订单 完整商城业务</li>
 *     <li>支付宝沙箱在线支付（alipay.trade.page.pay）</li>
 *     <li>智能客服 RAG：DeepSeek + DashScope text-embedding-v3 + Redis 向量库</li>
 *     <li>知识库管理：txt/doc/pdf/markdown 上传、解析、向量化</li>
 * </ul>
 *
 * @author zyr
 */
@SpringBootApplication
public class ZyrShopApplication {

    /**
     * 程序入口方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ZyrShopApplication.class, args);
    }
}
