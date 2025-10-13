package io.renren.crmchat.service.storage;

import io.renren.crmchat.exception.CrmChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储实现
 * 参考 PHP: crmeb/services/upload/storage/Local.php
 *
 * @author CRMChat Team
 */
@Slf4j
@Component("localFileStorage")
public class LocalFileStorage implements FileStorageStrategy {

    /**
     * 上传根目录（从配置读取，默认：./uploads）
     */
    @Value("${file.upload.path:./uploads}")
    private String uploadBasePath;

    /**
     * 文件访问域名（从配置读取）
     */
    @Value("${file.upload.domain:http://localhost:20108}")
    private String uploadDomain;

    @Override
    public String uploadFile(MultipartFile file, String appId, String module) {
        if (file == null || file.isEmpty()) {
            throw new CrmChatException("Upload file cannot be empty");
        }

        // 1. 生成文件保存路径：/uploads/{appId}/{module}/{yyyy-MM-dd}/{uuid}.{ext}
        String savePath = buildSavePath(appId, module);
        String fileName = generateFileName(file.getOriginalFilename());
        String fullPath = savePath + "/" + fileName;

        try {
            // 2. 构建目标文件路径
            Path targetPath = Paths.get(fullPath);
            File targetFile = targetPath.toFile();

            // 3. 确保父目录存在（使用 File.mkdirs() 更可靠）
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    log.info("Created upload directory: {}", parentDir.getAbsolutePath());
                } else {
                    log.warn("Failed to create directory or directory already exists: {}", parentDir.getAbsolutePath());
                }
            }

            // 4. 保存文件
            file.transferTo(targetFile);
            log.info("File uploaded successfully: {}", targetFile.getAbsolutePath());

            // 5. 返回访问URL（使用相对于uploadBasePath的路径）
            // 从完整路径中提取相对路径：/{appId}/{module}/{date}/{filename}
            Path baseAbsolutePath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
            Path fileAbsolutePath = targetFile.toPath().toAbsolutePath().normalize();
            Path relativePath = baseAbsolutePath.relativize(fileAbsolutePath);

            // 确保使用正斜杠（URL格式）
            String relativePathStr = "/" + relativePath.toString().replace("\\", "/");
            return getFileUrl(relativePathStr);

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            throw new CrmChatException("File upload failed: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            // 移除域名前缀，获取本地路径
            String localPath = filePath.replace(uploadDomain, "");
            if (!localPath.startsWith(uploadBasePath)) {
                localPath = uploadBasePath + localPath;
            }

            File file = new File(localPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("File deleted successfully: {}", localPath);
                }
                return deleted;
            } else {
                log.warn("File does not exist: {}", localPath);
                return false;
            }
        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // 确保路径以 / 开头
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        return uploadDomain + filePath;
    }

    @Override
    public boolean fileExists(String filePath) {
        String localPath = filePath.replace(uploadDomain, "");
        if (!localPath.startsWith(uploadBasePath)) {
            localPath = uploadBasePath + localPath;
        }
        return new File(localPath).exists();
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 构建文件保存路径
     * 格式：/uploads/{appId}/{module}/{yyyy-MM-dd}
     * 参考 PHP: Local.php::uploadDir()
     *
     * @param appId  租户ID
     * @param module 模块名称
     * @return 完整保存路径（绝对路径）
     */
    private String buildSavePath(String appId, String module) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 构建相对路径
        String relativePath = uploadBasePath + "/" + appId + "/" + module + "/" + dateDir;

        // 转换为绝对路径（确保路径一致性）
        Path absolutePath = Paths.get(relativePath).toAbsolutePath().normalize();
        return absolutePath.toString();
    }

    /**
     * 生成唯一文件名
     * 格式：{uuid}.{ext}
     * 参考 PHP: Local.php::createSaveFilePath()
     *
     * @param originalFilename 原始文件名
     * @return 新文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（小写）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        int lastDot = filename.lastIndexOf(".");
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
