package com.zyr.shop.controller;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.entity.KbChunk;
import com.zyr.shop.entity.KbDocument;
import com.zyr.shop.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理接口
 *
 * <p>路径前缀 {@code /api/knowledge}，由管理员后台 Vue 客户端调用</p>
 *
 * <p>接口：</p>
 * <ul>
 *     <li>POST /api/knowledge/upload  上传文档（自动解析+切分+向量化入库）</li>
 *     <li>GET  /api/knowledge/page    分页查询文档列表</li>
 *     <li>GET  /api/knowledge/{id}    文档详情</li>
 *     <li>GET  /api/knowledge/{id}/chunks 查看分块</li>
 *     <li>DELETE /api/knowledge/{id}  删除文档（同步删除向量）</li>
 * </ul>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    /** 知识库服务 */
    private final KnowledgeService knowledgeService;

    /**
     * 上传知识文档
     *
     * @param file 文档文件
     * @return 文档记录
     */
    @PostMapping("/upload")
    public Result<KbDocument> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(knowledgeService.upload(file));
    }

    /**
     * 分页查询文档列表
     *
     * @param page     页码
     * @param size     每页条数
     * @param keyword  文件名关键词
     * @param fileType 文件类型
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<KbDocument>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String fileType) {
        return Result.success(knowledgeService.page(page, size, keyword, fileType));
    }

    /**
     * 文档详情
     *
     * @param id 文档ID
     * @return 文档实体
     */
    @GetMapping("/{id}")
    public Result<KbDocument> detail(@PathVariable Long id) {
        return Result.success(knowledgeService.detail(id));
    }

    /**
     * 查看文档分块
     *
     * @param id 文档ID
     * @return 分块列表
     */
    @GetMapping("/{id}/chunks")
    public Result<List<KbChunk>> chunks(@PathVariable Long id) {
        return Result.success(knowledgeService.chunks(id));
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }
}
