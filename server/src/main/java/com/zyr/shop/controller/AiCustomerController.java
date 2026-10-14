package com.zyr.shop.controller;

import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 智能客服 SSE 流式问答接口
 *
 * <p>路径前缀 {@code /api/ai}，前台用户与管理员均可使用</p>
 *
 * <p>说明：浏览器原生 EventSource 仅支持 GET 且无法自定义请求头，
 * 因此问题与登录令牌均通过查询参数传递（JwtInterceptor 已支持 token 查询参数）</p>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiCustomerController {

    /** RAG 问答服务 */
    private final RagService ragService;

    /**
     * 智能客服问答（SSE 流式输出）
     *
     * <p>每个文本分片作为普通 message 事件下发；
     * 流末尾追加一个 [DONE] 标记（同样作为默认 message 事件下发），
     * 前端 onmessage 收到 "[DONE]" 后关闭连接避免自动重连</p>
     *
     * @param prompt 用户问题
     * @return SSE 事件流
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestParam String prompt) {
        // 文本分片 -> 普通 message 事件
        Flux<ServerSentEvent<String>> contentStream = ragService
                .ask(prompt, UserContext.getUserId())
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
        // 流末尾追加 [DONE] 结束标记（作为默认 message 事件，前端 onmessage 可直接接收）
        ServerSentEvent<String> completeEvent = ServerSentEvent.<String>builder()
                .data("[DONE]")
                .build();
        return contentStream.concatWithValues(completeEvent);
    }
}
