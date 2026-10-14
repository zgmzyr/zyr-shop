package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.config.ZyrProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务
 *
 * <p>所有上传的文件（商品图片、客服知识库文档、用户头像）统一保存到 D:/uploads4，
 * 按"年月"分子目录存放，文件名使用 UUID 防止重名覆盖</p>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    /** 项目自定义配置 */
    private final ZyrProperties properties;

    /** 上传根目录 */
    private Path rootPath;

    /**
     * 启动时确保上传根目录存在
     */
    @PostConstruct
    public void init() {
        this.rootPath = Paths.get(properties.getUpload().getDir());
        try {
            Files.createDirectories(rootPath);
            log.info("【文件存储】上传根目录就绪：{}", rootPath);
        } catch (IOException e) {
            log.error("【文件存储】创建上传目录失败：{}", rootPath, e);
        }
    }

    /**
     * 保存上传文件
     *
     * @param file 前端上传的文件
     * @return 存储结果（绝对路径 + 访问URL + 原始文件名）
     */
    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 原始文件名与扩展名
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        // 子目录：yyyyMM
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 存储文件名：UUID + 原扩展名
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;

        Path targetDir = rootPath.resolve(month);
        Path targetFile = targetDir.resolve(storedName);
        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            log.error("【文件存储】文件保存失败：{}", originalName, e);
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        // 相对访问路径，配合 WebConfig 的静态资源映射使用
        String url = "/uploads/" + month + "/" + storedName;
        log.info("【文件存储】文件保存成功：{} -> {}", originalName, targetFile);
        return new StoredFile(targetFile.toString(), url, originalName);
    }

    /**
     * 删除磁盘上的文件（删除文档时调用，文件不存在则忽略）
     *
     * @param absolutePath 文件绝对路径
     */
    public void deleteQuietly(String absolutePath) {
        try {
            Files.deleteIfExists(Paths.get(absolutePath));
        } catch (IOException e) {
            log.warn("【文件存储】磁盘文件删除失败：{}", absolutePath);
        }
    }

    /**
     * 文件存储结果
     *
     * @param absolutePath 磁盘绝对路径
     * @param url          HTTP 访问相对路径
     * @param originalName 原始文件名
     */
    public record StoredFile(String absolutePath, String url, String originalName) {
    }
}
