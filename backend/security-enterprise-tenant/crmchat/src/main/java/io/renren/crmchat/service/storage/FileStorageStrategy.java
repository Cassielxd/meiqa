package io.renren.crmchat.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口
 * 参考 PHP: crmeb/basic/BaseUpload.php
 *
 * 策略模式：支持本地存储、OSS、COS、七牛云等多种存储方式
 *
 * @author CRMChat Team
 */
public interface FileStorageStrategy {

    /**
     * 上传文件
     *
     * @param file     上传的文件
     * @param appId    租户ID（用于多租户文件隔离）
     * @param module   模块名称（如：avatar, chat, product等）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String appId, String module);

    /**
     * 删除文件
     *
     * @param filePath 文件路径或URL
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);

    /**
     * 获取文件访问URL
     *
     * @param filePath 文件相对路径
     * @return 完整的访问URL
     */
    String getFileUrl(String filePath);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean fileExists(String filePath);

    /**
     * 获取存储类型名称
     *
     * @return local, oss, cos, qiniu等
     */
    String getStorageType();
}
