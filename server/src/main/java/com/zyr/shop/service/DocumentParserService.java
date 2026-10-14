package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文档解析服务
 *
 * <p>基于 Apache Tika 自动识别并解析 txt、doc/docx、pdf、markdown 等格式，
 * 统一抽取为纯文本，供后续切分与向量化使用（智能客服知识库入库使用）</p>
 *
 * @author zyr
 */
@Slf4j
@Service
public class DocumentParserService {

    /**
     * 解析文档为纯文本
     *
     * @param filePath 文档在磁盘上的路径
     * @param fileName 原始文件名（帮助 Tika 识别格式）
     * @return 抽取到的纯文本
     */
    public String parse(Path filePath, String fileName) {
        // BodyContentHandler 传入 -1 表示不限制写入长度（默认只保留前10万字符）
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        // 提供文件名，辅助 Tika 根据扩展名判断 MIME 类型
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
        try (InputStream stream = Files.newInputStream(filePath)) {
            log.info("【文档解析】节点1-开始解析文件：{}", fileName);
            new AutoDetectParser().parse(stream, handler, metadata, new ParseContext());
            String text = handler.toString();
            log.info("【文档解析】节点2-解析完成，提取字符数：{}", text.length());
            return text;
        } catch (Exception e) {
            log.error("【文档解析】文件解析失败：{}", fileName, e);
            throw new BusinessException("文件解析失败，请确认文件未损坏且格式受支持：" + e.getMessage());
        }
    }
}
