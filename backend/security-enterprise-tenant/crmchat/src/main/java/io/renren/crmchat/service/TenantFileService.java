package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.SystemAttachmentCategoryMapper;
import io.renren.crmchat.dao.SystemAttachmentMapper;
import io.renren.crmchat.entity.SystemAttachmentCategoryEntity;
import io.renren.crmchat.entity.SystemAttachmentEntity;
import io.renren.crmchat.service.common.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant 文件管理服务
 * PHP Reference: /app/controller/tenant/file/AttachmentCategory.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getFileCategoryList(): 获取附件分类列表
 *    - 支持name模糊查询
 * 2. createFileCategory(): 创建附件分类
 *    - 验证name非空
 *    - 设置pid（父分类ID）
 * 3. getFileCategoryDetail(): 获取附件分类详情
 *    - 通过id查询
 * 4. updateFileCategory(): 更新附件分类
 *    - 验证name非空
 *    - 如果有子分类，不能修改pid
 * 5. deleteFileCategory(): 删除附件分类
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantFileService {

    private final SystemAttachmentCategoryMapper attachmentCategoryMapper;
    private final SystemAttachmentMapper systemAttachmentMapper;
    private final FileService fileService;

    /**
     * 获取附件分类列表
     * GET /api/tenant/file/category
     *
     * PHP Reference: AttachmentCategory.php::index()
     *
     * 业务逻辑:
     * 1. 接收过滤参数（name）
     * 2. 查询所有分类或按name模糊搜索
     * 3. 返回分类列表
     *
     * @param filters 过滤条件（name）
     * @return 分类列表
     */
    public List<SystemAttachmentCategoryEntity> getFileCategoryList(Map<String, Object> filters) {
        // PHP: $where = $this->request->getMore([['name', '']]);
        // PHP: return $this->success($this->service->getAll($where));

        QueryWrapper<SystemAttachmentCategoryEntity> wrapper = new QueryWrapper<>();

        // 可选name过滤
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().trim().isEmpty()) {
            wrapper.like("name", filters.get("name").toString());
        }

        return attachmentCategoryMapper.selectList(wrapper);
    }

    /**
     * 获取附件分类详情
     * GET /api/tenant/file/category/:id
     *
     * PHP Reference: Resource route - read()
     *
     * 业务逻辑:
     * 1. 通过id查询分类
     * 2. 返回分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    public SystemAttachmentCategoryEntity getFileCategoryDetail(Integer id) {
        SystemAttachmentCategoryEntity category = attachmentCategoryMapper.selectById(id);

        if (category == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Category does not exist");
        }

        return category;
    }

    /**
     * 创建附件分类
     * POST /api/tenant/file/category
     *
     * PHP Reference: AttachmentCategory.php::save()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 设置pid（父分类ID，默认0）
     * 3. 保存到数据库
     *
     * @param data 分类数据（pid, name）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createFileCategory(Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        // 2. PHP: $data = ['pid' => 0, 'name' => ''];
        SystemAttachmentCategoryEntity category = new SystemAttachmentCategoryEntity();
        category.setName(data.get("name").toString());

        if (data.containsKey("pid") && data.get("pid") != null && !data.get("pid").toString().isEmpty()) {
            category.setPid(Integer.parseInt(data.get("pid").toString()));
        } else {
            category.setPid(0); // 默认顶级分类
        }

        // 3. PHP: $this->service->save($data);
        int result = attachmentCategoryMapper.insert(category);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
        }
    }

    /**
     * 更新附件分类
     * PUT /api/tenant/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::update()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 获取当前分类信息
     * 3. 检查是否有子分类
     * 4. 如果有子分类且要修改pid，返回错误
     * 5. 更新分类
     *
     * @param id 分类ID
     * @param data 分类数据（pid, name）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileCategory(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        // 2. PHP: $info = $this->service->get($id);
        SystemAttachmentCategoryEntity category = attachmentCategoryMapper.selectById(id);
        if (category == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Category does not exist");
        }

        // 3. PHP: $count = $this->service->count(['pid' => $id]);
        QueryWrapper<SystemAttachmentCategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", id);
        Long childCount = attachmentCategoryMapper.selectCount(wrapper);

        // 4. PHP: if ($count && $info['pid'] != $data['pid']) return $this->fail('该分类有下级分类，无法修改上级');
        if (data.containsKey("pid") && data.get("pid") != null && !data.get("pid").toString().isEmpty()) {
            Integer newPid = Integer.parseInt(data.get("pid").toString());
            if (childCount > 0 && !category.getPid().equals(newPid)) {
                throw new io.renren.crmchat.exception.CrmChatException("This category has subcategories, cannot modify parent");
            }
            category.setPid(newPid);
        }

        // 5. PHP: $this->service->update($id, $data);
        category.setName(data.get("name").toString());

        int result = attachmentCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to edit category");
        }
    }

    /**
     * 删除附件分类
     * DELETE /api/tenant/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::delete()
     *
     * 业务逻辑:
     * 1. 删除分类
     *
     * @param id 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileCategory(Integer id) {
        // PHP: $this->service->del($id);
        int result = attachmentCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 获取图片附件列表
     * GET /api/tenant/file/file
     *
     * PHP Reference: Attachment.php::index()
     *
     * 业务逻辑:
     * 1. 接收pid参数（分类ID）
     * 2. 查询指定分类下的所有附件
     * 3. 返回附件列表
     *
     * @param filters 过滤条件（pid）
     * @return 附件列表
     */
    public List<SystemAttachmentEntity> getFileList(Map<String, Object> filters) {
        // PHP: $where = $this->request->getMore([['pid', 0]]);
        // PHP: return $this->success($this->service->getImageList($where));

        QueryWrapper<SystemAttachmentEntity> wrapper = new QueryWrapper<>();

        // 过滤条件：pid
        if (filters.containsKey("pid") && filters.get("pid") != null && !filters.get("pid").toString().isEmpty()) {
            wrapper.eq("pid", Integer.parseInt(filters.get("pid").toString()));
        } else {
            wrapper.eq("pid", 0); // 默认查询顶级分类
        }

        return systemAttachmentMapper.selectList(wrapper);
    }

    /**
     * 上传图片
     * POST /api/tenant/file/upload/:upload_type?
     *
     * PHP Reference: Attachment.php::upload()
     *
     * 业务逻辑:
     * 1. 接收文件和pid参数
     * 2. 使用FileService上传文件
     * 3. 保存附件记录到数据库
     * 4. 返回附件信息
     *
     * @param file       上传的文件
     * @param pid        分类ID
     * @param uploadType 上传类型：0-自动，1-本地，2-OSS
     * @return 附件信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadFile(MultipartFile file, Integer pid, Integer uploadType) {
        // 1. 如果uploadType=0，使用系统配置
        if (uploadType == 0) {
            uploadType = 1; // 默认本地上传
        }

        // 2. 使用FileService上传文件
        String filePath;
        try {
            filePath = fileService.uploadFile(file, "tenant", "attach");
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new io.renren.crmchat.exception.CrmChatException("File upload failed: " + e.getMessage());
        }

        // 3. 获取文件信息
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();

        // 4. 创建附件记录
        SystemAttachmentEntity attachment = new SystemAttachmentEntity();
        attachment.setName(originalFilename);
        attachment.setRealName(originalFilename);
        attachment.setAttDir(filePath);
        attachment.setSattDir(filePath); // 缩略图路径（与原图相同）
        attachment.setAttSize(String.valueOf(fileSize));
        attachment.setAttType(contentType);
        attachment.setImageType(uploadType);
        attachment.setModuleType(1); // 1-附件模块
        attachment.setTime((int) (System.currentTimeMillis() / 1000));
        attachment.setPid(pid);

        // 5. 保存到数据库
        int result = systemAttachmentMapper.insert(attachment);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save attachment record");
        }

        // 6. 返回附件信息（PHP格式）
        Map<String, Object> response = new HashMap<>();
        response.put("src", attachment.getAttDir()); // PHP返回src字段
        response.put("att_id", attachment.getAttId());
        response.put("name", attachment.getName());
        response.put("att_dir", attachment.getAttDir());
        response.put("satt_dir", attachment.getSattDir());
        response.put("att_size", attachment.getAttSize());
        response.put("att_type", attachment.getAttType());
        response.put("image_type", attachment.getImageType());

        return response;
    }

    /**
     * 删除图片
     * POST /api/tenant/file/file/delete
     *
     * PHP Reference: Attachment.php::delete()
     *
     * 业务逻辑:
     * 1. 接收ids参数（支持批量删除，逗号分隔）
     * 2. 解析ID列表
     * 3. 遍历删除每个附件
     *
     * @param ids 附件ID列表（逗号分隔字符串）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(String ids) {
        // PHP: $ids = $this->request->postMore([['ids', '']], true);
        // PHP: $this->service->del($ids);

        if (ids == null || ids.trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select file to delete");
        }

        // 解析ID列表
        List<String> idList = Arrays.asList(ids.split(","));
        List<Integer> idIntList = new ArrayList<>();
        for (String id : idList) {
            if (id != null && !id.trim().isEmpty()) {
                idIntList.add(Integer.parseInt(id.trim()));
            }
        }

        if (idIntList.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select file to delete");
        }

        // 批量删除
        int result = systemAttachmentMapper.deleteBatchIds(idIntList);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 修改图片名称
     * PUT /api/tenant/file/file/update/:id
     *
     * PHP Reference: Attachment.php::update()
     *
     * 业务逻辑:
     * 1. 验证real_name非空
     * 2. 更新附件的real_name字段
     *
     * @param id       附件ID
     * @param realName 新文件名
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileName(Integer id, String realName) {
        // PHP: $realName = $this->request->post('real_name', '');
        // PHP: if (!$realName) return $this->fail('文件名称不能为空');

        if (realName == null || realName.trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("File name cannot be empty");
        }

        // 查询附件是否存在
        SystemAttachmentEntity attachment = systemAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Attachment does not exist");
        }

        // PHP: $this->service->update($id, ['real_name' => $realName]);
        attachment.setRealName(realName);

        int result = systemAttachmentMapper.updateById(attachment);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 移动图片分类
     * PUT /api/tenant/file/file/do_move
     *
     * PHP Reference: Attachment.php::moveImageCate()
     *
     * 业务逻辑:
     * 1. 接收pid和images参数
     * 2. 批量更新图片的分类ID
     *
     * @param pid    新的分类ID
     * @param images 图片ID列表（逗号分隔字符串）
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveFiles(Integer pid, String images) {
        // PHP: $data = $this->request->postMore([['pid', 0], ['images', '']]);
        // PHP: $this->service->move($data);

        if (images == null || images.trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select image to move");
        }

        // 解析图片ID列表
        List<String> imageList = Arrays.asList(images.split(","));
        List<Integer> imageIds = new ArrayList<>();
        for (String image : imageList) {
            if (image != null && !image.trim().isEmpty()) {
                imageIds.add(Integer.parseInt(image.trim()));
            }
        }

        if (imageIds.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select image to move");
        }

        // 批量更新pid
        for (Integer imageId : imageIds) {
            SystemAttachmentEntity attachment = systemAttachmentMapper.selectById(imageId);
            if (attachment != null) {
                attachment.setPid(pid);
                systemAttachmentMapper.updateById(attachment);
            }
        }
    }
}
