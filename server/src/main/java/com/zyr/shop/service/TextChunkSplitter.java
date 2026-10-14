package com.zyr.shop.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本切分器
 *
 * <p>RAG 入库前需要把长文档切成语义完整、长度适中的小块。
 * 本实现面向中英文混合场景：先按句子/换行边界切分，再按固定长度贪心打包，
 * 相邻分块之间保留少量重叠（overlap），避免关键信息被拦腰截断</p>
 *
 * @author zyr
 */
@Component
public class TextChunkSplitter {

    /** 每个分块的最大字符数 */
    private static final int CHUNK_SIZE = 500;

    /** 相邻分块的重叠字符数 */
    private static final int OVERLAP_SIZE = 80;

    /**
     * 将整篇文档切分为文本块
     *
     * @param rawText 文档原始纯文本
     * @return 分块文本列表
     */
    public List<String> split(String rawText) {
        List<String> chunks = new ArrayList<>();
        if (rawText == null || rawText.isBlank()) {
            return chunks;
        }

        // 1. 文本预处理：统一换行、去除多余空白
        String text = rawText.replace("\r", "")
                .replaceAll("[\\t\\u00A0]", " ")
                .replaceAll(" {3,}", "  ")
                .trim();

        // 2. 按中英文句读符号与换行切分为最小语义片段（保留分隔符）
        String[] sentences = text.split("(?<=[。！？；!?;\\n])");
        List<String> segments = new ArrayList<>();
        for (String sentence : sentences) {
            String s = sentence.trim();
            if (s.isEmpty()) {
                continue;
            }
            // 单个句子超长时按固定长度硬切分
            if (s.length() <= CHUNK_SIZE) {
                segments.add(s);
            } else {
                for (int i = 0; i < s.length(); i += CHUNK_SIZE) {
                    segments.add(s.substring(i, Math.min(i + CHUNK_SIZE, s.length())));
                }
            }
        }

        // 3. 贪心打包：尽量把多个短句拼进同一个分块
        StringBuilder current = new StringBuilder();
        for (String segment : segments) {
            if (current.length() + segment.length() + 1 > CHUNK_SIZE && !current.isEmpty()) {
                chunks.add(current.toString());
                // 新分块以"上一块末尾的若干字符"开头，形成重叠
                String tail = current.substring(Math.max(0, current.length() - OVERLAP_SIZE));
                current = new StringBuilder();
                if (tail.length() + segment.length() <= CHUNK_SIZE) {
                    current.append(tail);
                }
            }
            if (!current.isEmpty()) {
                current.append('\n');
            }
            current.append(segment);
        }
        if (!current.toString().isBlank()) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
