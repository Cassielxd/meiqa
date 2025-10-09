package io.renren.crmchat.service.common;

import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.storage.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文件上传服务（策略模式）
 * 参考 PHP: crmeb/services/UploadService.php
 *
 * 设计模式：Strategy Pattern（策略模式）
 * - FileStorageStrategy: 存储策略接口
 * - LocalFileStorage: 本地存储实现
 * - OssFileStorage: 阿里云OSS实现
 * - FileService: 策略上下文，根据配置选择存储方式
 *
 * 支持的存储类型：
 * - local: 本地存储（默认）
 * - oss: 阿里云OSS
 * - cos: 腾讯云COS（待实现）
 * - qiniu: 七牛云（待实现）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
public class FileService {

    /**
     * 存储类型配置：local, oss, cos, qiniu
     */
    @Value("${file.storage.type:local}")
    private String storageType;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * 允许的文件格式
     */
    private static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "rar", "7z", "txt", "mp4", "mp3"
    );

    /**
     * 最大文件大小（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 存储策略集合（Spring自动注入所有FileStorageStrategy实现）
     */
    private final Map<String, FileStorageStrategy> storageStrategies;

    @Autowired
    public FileService(Map<String, FileStorageStrategy> storageStrategies) {
        this.storageStrategies = storageStrategies;
    }

    /**
     * 上传文件
     * 参考 PHP: UploadService.php::init()
     *
     * @param file   上传的文件
     * @param appId  租户ID（用于多租户文件隔离）
     * @param module 模块名称（如：avatar, chat, product等）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String appId, String module) {
        // 1. 文件验证
        validateFile(file);

        // 2. 获取存储策略
        FileStorageStrategy strategy = getStorageStrategy();

        // 3. 执行上传
        try {
            String fileUrl = strategy.uploadFile(file, appId, module);
            log.info("文件上传成功 - 策略: {}, AppID: {}, Module: {}, URL: {}",
                    strategy.getStorageType(), appId, module, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败 - 策略: {}, 错误: {}", strategy.getStorageType(), e.getMessage(), e);
            throw new CrmChatException("File upload failed: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径或URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        FileStorageStrategy strategy = getStorageStrategy();
        return strategy.deleteFile(filePath);
    }

    /**
     * 获取文件访问URL
     *
     * @param filePath 文件相对路径
     * @return 完整的访问URL
     */
    public String getFileUrl(String filePath) {
        FileStorageStrategy strategy = getStorageStrategy();
        return strategy.getFileUrl(filePath);
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public boolean fileExists(String filePath) {
        FileStorageStrategy strategy = getStorageStrategy();
        return strategy.fileExists(filePath);
    }

    /**
     * 获取当前使用的存储策略
     * 参考 PHP: UploadService.php::init()
     *
     * @return 存储策略实现
     */
    private FileStorageStrategy getStorageStrategy() {
        // 根据配置选择存储策略（类似PHP中的switch）
        String strategyBeanName = storageType + "FileStorage";
        FileStorageStrategy strategy = storageStrategies.get(strategyBeanName);

        if (strategy == null) {
            log.warn("未找到存储策略: {}, 使用默认本地存储", storageType);
            strategy = storageStrategies.get("localFileStorage");
        }

        if (strategy == null) {
            throw new CrmChatException("File storage service not configured");
        }

        return strategy;
    }

    /**
     * 文件验证
     *
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CrmChatException("Upload file cannot be empty");
        }

        // 1. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CrmChatException("File size cannot exceed 10MB");
        }

        // 2. 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CrmChatException("Invalid file name");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new CrmChatException("Unsupported file format: " + extension);
        }
    }

    /**
     * 验证图片文件
     *
     * @param file 上传的文件
     */
    public void validateImageFile(MultipartFile file) {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new CrmChatException("Only image formats supported: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（小写）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
