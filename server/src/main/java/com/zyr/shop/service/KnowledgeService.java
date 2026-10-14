package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.common.PageResult;
import com.zyr.shop.entity.KbChunk;
import com.zyr.shop.entity.KbDocument;
import com.zyr.shop.repository.KbChunkRepository;
import com.zyr.shop.repository.KbDocumentRepository;
import com.zyr.shop.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 知识库服务（智能客服 RAG 入库）
 *
 * <p>链路：上传 → Tika 解析 → 切分 → 调 DashScope text-embedding-v3 向量化 → 写入 Redis 向量库 → 落 MySQL</p>
 *
 * <p>本系统向量索引 zyr_shop_kb_index 与既有 RAG 项目隔离，互不影响</p>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    /** 允许上传的扩展名 -> 系统内文件类型映射 */
    private static final Map<String, String> SUPPORTED_TYPES = Map.of(
            "txt", "TXT",
            "doc", "DOC",
            "docx", "DOC",
            "pdf", "PDF",
            "md", "MD",
            "markdown", "MD");

    /** Redis 向量库 */
    private final VectorStore vectorStore;

    /** 文档数据访问 */
    private final KbDocumentRepository documentRepository;

    /** 分块数据访问 */
    private final KbChunkRepository chunkRepository;

    /** 文件存储服务 */
    private final FileStorageService fileStorageService;

    /** 文档解析服务 */
    private final DocumentParserService documentParserService;

    /** 文本切分器 */
    private final TextChunkSplitter textChunkSplitter;

    /**
     * 上传知识文档：保存文件 → 解析 → 切分 → 向量化 → 落库
     *
     * @param file 上传文件
     * @return 文档实体
     */
    public KbDocument upload(MultipartFile file) {
        // 1. 文件类型校验
        String originalName = file.getOriginalFilename();
        String fileType = resolveFileType(originalName);

        // 2. 保存文件到 D:/uploads4
        FileStorageService.StoredFile stored = fileStorageService.store(file);

        // 3. 落库一条 PROCESSING 记录
        KbDocument document = new KbDocument();
        document.setFileName(originalName);
        document.setFilePath(stored.absolutePath());
        document.setFileUrl(stored.url());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setStatus("PROCESSING");
        document.setUploaderId(UserContext.getUserId());
        document = documentRepository.save(document);
        log.info("【知识库入库】文档[{}]开始处理 id={}", originalName, document.getId());

        try {
            // 4. Tika 解析为纯文本
            String text = documentParserService.parse(java.nio.file.Path.of(stored.absolutePath()), originalName);
            if (text == null || text.isBlank()) {
                throw new BusinessException("未从文件中解析出任何文本内容");
            }

            // 5. 文本切分
            List<String> chunkTexts = textChunkSplitter.split(text);
            if (chunkTexts.isEmpty()) {
                throw new BusinessException("文本切分结果为空");
            }
            log.info("【知识库入库】文档[{}]切分为 {} 个分块", originalName, chunkTexts.size());

            // 6. 向量化并写入 Redis（vectorStore.add 内部会调嵌入模型批量生成向量）
            List<Document> vectorDocuments = new ArrayList<>();
            List<KbChunk> chunkEntities = new ArrayList<>();
            for (int i = 0; i < chunkTexts.size(); i++) {
                String vectorId = UUID.randomUUID().toString();
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("docId", String.valueOf(document.getId()));
                metadata.put("docName", originalName);
                metadata.put("chunkNo", i + 1);
                vectorDocuments.add(new Document(vectorId, chunkTexts.get(i), metadata));

                KbChunk chunk = new KbChunk();
                chunk.setDocumentId(document.getId());
                chunk.setChunkNo(i + 1);
                chunk.setVectorId(vectorId);
                chunk.setContent(chunkTexts.get(i));
                chunkEntities.add(chunk);
            }
            vectorStore.add(vectorDocuments);
            log.info("【知识库入库】文档[{}]向量写入 Redis 完成 共 {} 条", originalName, vectorDocuments.size());

            // 7. 分块映射与文档状态落库
            chunkRepository.saveAll(chunkEntities);
            document.setChunkCount(chunkEntities.size());
            document.setStatus("READY");
            documentRepository.save(document);
            log.info("【知识库入库】文档[{}]入库完成", originalName);
            return document;
        } catch (Exception e) {
            log.error("【知识库入库】文档[{}]处理失败", originalName, e);
            document.setStatus("FAILED");
            documentRepository.save(document);
            throw new BusinessException("文档处理失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询知识库文档
     *
     * @param page     页码
     * @param size     每页条数
     * @param keyword  文件名关键词
     * @param fileType 文件类型过滤
     * @return 分页结果
     */
    public PageResult<KbDocument> page(int page, int size, String keyword, String fileType) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return PageResult.of(documentRepository.search(keyword, fileType, pageable));
    }

    /**
     * 文档详情
     *
     * @param id 文档ID
     * @return 文档实体
     */
    public KbDocument detail(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
    }

    /**
     * 查看文档切分出的全部分块
     *
     * @param documentId 文档ID
     * @return 分块列表
     */
    public List<KbChunk> chunks(Long documentId) {
        return chunkRepository.findByDocumentIdOrderByChunkNoAsc(documentId);
    }

    /**
     * 删除文档：同步删除 Redis 向量、MySQL 分块记录、磁盘原文件
     *
     * @param id 文档ID
     */
    @Transactional
    public void delete(Long id) {
        KbDocument document = detail(id);
        // 普通用户只能删除自己上传的文档，管理员可删除任意文档
        if (!UserContext.isAdmin()
                && (document.getUploaderId() == null
                || !document.getUploaderId().equals(UserContext.getUserId()))) {
            throw new BusinessException(403, "只能删除自己上传的文档");
        }
        // 1. 删除 Redis 向量（按入库时保存的 vectorId 精准删除）
        List<KbChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkNoAsc(id);
        if (!chunks.isEmpty()) {
            List<String> vectorIds = chunks.stream().map(KbChunk::getVectorId).toList();
            vectorStore.delete(vectorIds);
            log.info("【知识库删除】文档[{}]的 {} 条向量已删除", document.getFileName(), vectorIds.size());
        }
        // 2. 删除 MySQL 记录
        chunkRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);
        // 3. 删除磁盘文件
        fileStorageService.deleteQuietly(document.getFilePath());
        log.info("【知识库删除】文档[{}]删除完成", document.getFileName());
    }

    /**
     * 解析文件类型
     *
     * @param fileName 原始文件名
     * @return 系统内文件类型
     */
    private String resolveFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException("无法识别的文件类型，仅支持 txt、doc、docx、pdf、md、markdown");
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String type = SUPPORTED_TYPES.get(ext);
        if (type == null) {
            throw new BusinessException("不支持的文件格式：" + ext + "，仅支持 txt、doc、docx、pdf、md、markdown");
        }
        return type;
    }

    /**
     * 获取系统支持的全部文件类型
     *
     * @return 类型集合
     */
    public Set<String> supportedTypes() {
        return Set.copyOf(SUPPORTED_TYPES.values());
    }
}
