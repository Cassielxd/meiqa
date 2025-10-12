package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.crmchat.dao.SystemAttachmentCategoryMapper;
import io.renren.crmchat.dao.SystemAttachmentMapper;
import io.renren.crmchat.entity.SystemAttachmentCategoryEntity;
import io.renren.crmchat.entity.SystemAttachmentEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.FileService;
import io.renren.crmchat.service.common.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Admin 附件管理服务
 * PHP Reference: /app/services/system/attachment/SystemAttachmentServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getImageList(): 获取附件列表，添加site_url前缀到路径
 * 2. upload(): 文件上传，生成缩略图，保存到数据库
 * 3. del(): 删除附件（同时删除物理文件）
 * 4. update(): 更新文件名（real_name字段）
 * 5. move(): 移动附件到不同分类
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
public class AdminFileService {

    private final SystemAttachmentMapper systemAttachmentMapper;
    private final SystemAttachmentCategoryMapper categoryMapper;
    private final PaginationService paginationService;
    private final FileService fileService;

    @Value("${site.url:}")
    private String siteUrl;

    public AdminFileService(SystemAttachmentMapper systemAttachmentMapper,
                           SystemAttachmentCategoryMapper categoryMapper,
                           PaginationService paginationService,
                           FileService fileService) {
        this.systemAttachmentMapper = systemAttachmentMapper;
        this.categoryMapper = categoryMapper;
        this.paginationService = paginationService;
        this.fileService = fileService;
    }

    /**
     * 获取附件列表
     * PHP Reference: SystemAttachmentServices::getImageList()
     *
     * 业务逻辑:
     * 1. 分页查询
     * 2. 按pid过滤分类
     * 3. 添加site_url前缀到att_dir和satt_dir（如果不是完整URL）
     * 4. 只返回module_type=1的附件
     *
     * @param params 查询参数
     * @return 分页数据
     */
    public PageData<Map<String, Object>> getFileList(Map<String, Object> params) {
        // 1. 提取分页参数
        PaginationService.PaginationParams pagination = paginationService.extractParams(params);

        // 2. 构建查询条件
        QueryWrapper<SystemAttachmentEntity> wrapper = buildFileQueryWrapper(params);

        // 3. 分页查询
        List<SystemAttachmentEntity> files = systemAttachmentMapper.selectList(wrapper
                .last(String.format("LIMIT %d OFFSET %d", pagination.getLimit(), pagination.getOffset()))
        );

        // 4. 查询总数（PHP: module_type=1）
        QueryWrapper<SystemAttachmentEntity> countWrapper = buildFileQueryWrapper(params);
        countWrapper.eq("module_type", 1);
        Long total = systemAttachmentMapper.selectCount(countWrapper);

        // 5. 处理数据：添加site_url前缀（PHP逻辑）
        List<Map<String, Object>> list = new ArrayList<>();
        for (SystemAttachmentEntity file : files) {
            Map<String, Object> item = entityToMap(file);

            // PHP: 如果配置了site_url，且路径不是完整URL，则添加前缀
            if (siteUrl != null && !siteUrl.isEmpty()) {
                String attDir = file.getAttDir();
                String sattDir = file.getSattDir();

                // 检查是否已包含site_url或http前缀
                if (attDir != null && !attDir.contains(siteUrl) && !attDir.startsWith("http")) {
                    item.put("att_dir", siteUrl + attDir);
                }

                if (sattDir != null && !sattDir.contains(siteUrl) && !sattDir.startsWith("http")) {
                    item.put("satt_dir", siteUrl + sattDir);
                }
            }

            list.add(item);
        }

        // 6. 返回分页数据
        return paginationService.createPageData(list, total);
    }

    /**
     * 上传文件
     * PHP Reference: SystemAttachmentServices::upload()
     *
     * 业务逻辑:
     * 1. 验证文件类型
     * 2. 使用FileService上传文件（生成路径和缩略图）
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
        // 1. 如果uploadType=0，使用系统配置（PHP: sys_config('upload_type', 1)）
        if (uploadType == 0) {
            uploadType = 1; // 默认本地上传
        }

        // 2. 使用FileService上传文件
        String filePath;
        try {
            filePath = fileService.uploadFile(file, "admin", "attach");
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new CrmChatException("File upload failed: " + e.getMessage());
        }

        // 3. 获取文件信息
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();

        // 4. 创建附件记录（PHP逻辑）
        SystemAttachmentEntity attachment = new SystemAttachmentEntity();
        attachment.setName(originalFilename);
        attachment.setRealName(originalFilename);
        attachment.setAttDir(filePath);
        attachment.setSattDir(filePath); // 缩略图路径（简化版，与原图相同）
        attachment.setAttSize(String.valueOf(fileSize));
        attachment.setAttType(contentType);
        attachment.setImageType(uploadType);
        attachment.setModuleType(1); // 1-附件模块
        attachment.setTime((int) (System.currentTimeMillis() / 1000));
        attachment.setPid(pid);

        // 5. 保存到数据库
        int result = systemAttachmentMapper.insert(attachment);
        if (result <= 0) {
            throw new CrmChatException("Failed to save attachment record");
        }

        // 6. 返回附件信息（PHP格式）
        Map<String, Object> response = new HashMap<>();
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
     * 删除附件
     * PHP Reference: SystemAttachmentServices::del()
     *
     * 业务逻辑:
     * 1. 解析IDs（支持逗号分隔字符串或数组）
     * 2. 遍历删除每个附件
     * 3. 删除物理文件（使用FileService）
     * 4. 删除数据库记录
     *
     * @param idsObj IDs（可以是数组或逗号分隔字符串）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(Object idsObj) {
        // 1. 解析IDs（PHP: explode(',', $ids)）
        List<Integer> ids = parseIds(idsObj);

        if (ids.isEmpty()) {
            throw new CrmChatException("Please select image to delete");
        }

        // 2. 遍历删除
        for (Integer id : ids) {
            SystemAttachmentEntity attachment = systemAttachmentMapper.selectById(id);
            if (attachment != null) {
                // 3. 删除物理文件（PHP逻辑）
                try {
                    String attDir = attachment.getAttDir();
                    if (attDir != null && !attDir.isEmpty()) {
                        // PHP: 如果是本地文件（image_type=1），删除物理文件
                        if (attachment.getImageType() == 1) {
                            // 移除开头的 "/"
                            if (attDir.startsWith("/")) {
                                attDir = attDir.substring(1);
                            }
                            fileService.deleteFile(attDir);
                        }
                        // TODO: 其他存储类型的删除逻辑（OSS, 七牛云等）
                    }
                } catch (Exception e) {
                    // PHP: 捕获异常但不抛出，继续删除数据库记录
                    log.warn("Failed to delete physical file: {}", e.getMessage());
                }

                // 4. 删除数据库记录
                systemAttachmentMapper.deleteById(id);
            }
        }
    }

    /**
     * 更新文件名
     * PHP Reference: SystemAttachmentServices::update()
     *
     * 业务逻辑:
     * 1. 检查附件是否存在
     * 2. 更新real_name字段
     *
     * @param id      附件ID
     * @param newName 新文件名
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileName(Integer id, String newName) {
        // 1. 检查附件是否存在
        SystemAttachmentEntity attachment = systemAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new CrmChatException("Attachment does not exist");
        }

        // 2. 更新real_name（PHP字段名）
        attachment.setRealName(newName);

        int result = systemAttachmentMapper.updateById(attachment);
        if (result <= 0) {
            throw new CrmChatException("Failed to modify");
        }
    }

    /**
     * 移动附件到不同分类
     * PHP Reference: SystemAttachmentServices::move()
     *
     * 业务逻辑:
     * 1. 解析IDs
     * 2. 批量更新pid字段
     * 3. PHP: 不能重复移动到同一分类
     *
     * @param idsObj    IDs（可以是数组或逗号分隔字符串）
     * @param targetPid 目标分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveFiles(Object idsObj, Integer targetPid) {
        // 1. 解析IDs
        List<Integer> ids = parseIds(idsObj);

        if (ids.isEmpty()) {
            throw new CrmChatException("Please select image to move");
        }

        // 2. 批量更新pid
        for (Integer id : ids) {
            SystemAttachmentEntity attachment = systemAttachmentMapper.selectById(id);
            if (attachment != null) {
                // PHP: 检查是否移动到同一分类
                if (attachment.getPid().equals(targetPid)) {
                    throw new CrmChatException("Move failed or cannot move to the same category repeatedly");
                }

                attachment.setPid(targetPid);
                systemAttachmentMapper.updateById(attachment);
            }
        }
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<SystemAttachmentEntity> buildFileQueryWrapper(Map<String, Object> params) {
        QueryWrapper<SystemAttachmentEntity> wrapper = new QueryWrapper<>();

        // 分类ID筛选（PHP: where pid）
        if (params.containsKey("pid") && params.get("pid") != null) {
            String pidStr = params.get("pid").toString();
            if (!pidStr.isEmpty()) {
                try {
                    int pid = Integer.parseInt(pidStr);
                    wrapper.eq("pid", pid);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 文件名搜索
        if (params.containsKey("name") && params.get("name") != null) {
            String name = params.get("name").toString().trim();
            if (!name.isEmpty()) {
                wrapper.and(w -> w
                        .like("name", name)
                        .or().like("real_name", name)
                );
            }
        }

        // PHP: module_type = 1（附件模块）
        wrapper.eq("module_type", 1);

        // 默认按时间倒序
        wrapper.orderByDesc("time");

        return wrapper;
    }

    /**
     * 解析IDs（支持数组或逗号分隔字符串）
     * PHP: explode(',', $ids)
     */
    private List<Integer> parseIds(Object idsObj) {
        List<Integer> ids = new ArrayList<>();

        if (idsObj instanceof String) {
            // 逗号分隔字符串: "1,2,3"
            String idsStr = (String) idsObj;
            if (!idsStr.isEmpty()) {
                for (String idStr : idsStr.split(",")) {
                    try {
                        ids.add(Integer.parseInt(idStr.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } else if (idsObj instanceof List) {
            // 数组: [1, 2, 3]
            @SuppressWarnings("unchecked")
            List<?> idsList = (List<?>) idsObj;
            for (Object obj : idsList) {
                try {
                    ids.add(Integer.parseInt(obj.toString()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return ids;
    }

    /**
     * 实体转Map
     */
    private Map<String, Object> entityToMap(SystemAttachmentEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("att_id", entity.getAttId());
        map.put("name", entity.getName());
        map.put("real_name", entity.getRealName());
        map.put("att_dir", entity.getAttDir());
        map.put("satt_dir", entity.getSattDir());
        map.put("att_size", entity.getAttSize());
        map.put("att_type", entity.getAttType());
        map.put("image_type", entity.getImageType());
        map.put("module_type", entity.getModuleType());
        map.put("time", entity.getTime());
        map.put("pid", entity.getPid());
        return map;
    }

    // ==================== 附件分类管理业务逻辑 ====================

    /**
     * 获取分类列表（树形结构）
     * PHP Reference: SystemAttachmentCategoryServices::getAll()
     *
     * 业务逻辑:
     * 1. 如果有name搜索，查找所有匹配的分类
     * 2. 如果有name搜索，找到所有匹配分类的父分类，合并去重
     * 3. 使用tidyMenuTier()转换为树形结构
     * 4. 添加title字段（= name）
     * 5. 添加children字段和expand字段
     *
     * @param params 查询参数
     * @return { "list": [...] }
     */
    public Map<String, Object> getCategoryList(Map<String, Object> params) {
        // 1. 构建查询条件
        QueryWrapper<SystemAttachmentCategoryEntity> wrapper = new QueryWrapper<>();

        String name = "";
        if (params != null && params.containsKey("name") && params.get("name") != null) {
            name = params.get("name").toString().trim();
            if (!name.isEmpty()) {
                wrapper.like("name", name);
            }
        }

        // 2. 查询分类列表
        List<SystemAttachmentCategoryEntity> categoryList = categoryMapper.selectList(wrapper);

        // 3. PHP逻辑: 如果有name搜索，需要找到所有父分类并合并
        if (!name.isEmpty()) {
            // 收集所有pid
            Set<Integer> pids = new HashSet<>();
            for (SystemAttachmentCategoryEntity category : categoryList) {
                if (category.getPid() != null && category.getPid() > 0) {
                    pids.add(category.getPid());
                }
            }

            // 查询所有父分类
            if (!pids.isEmpty()) {
                QueryWrapper<SystemAttachmentCategoryEntity> parentWrapper = new QueryWrapper<>();
                parentWrapper.in("id", pids);
                List<SystemAttachmentCategoryEntity> parentList = categoryMapper.selectList(parentWrapper);

                // 合并并去重（PHP: array_merge + 去重逻辑）
                Set<Integer> existingIds = new HashSet<>();
                for (SystemAttachmentCategoryEntity cat : categoryList) {
                    existingIds.add(cat.getId());
                }

                for (SystemAttachmentCategoryEntity parent : parentList) {
                    if (!existingIds.contains(parent.getId())) {
                        categoryList.add(parent);
                        existingIds.add(parent.getId());
                    }
                }
            }
        }

        // 4. 转换为树形结构（PHP: tidyMenuTier()）
        List<Map<String, Object>> treeList = tidyMenuTier(categoryList, 0);

        // 5. 返回格式: { "list": [...] }
        Map<String, Object> result = new HashMap<>();
        result.put("list", treeList);
        return result;
    }

    /**
     * 转换为树形结构
     * PHP Reference: SystemAttachmentCategoryServices::tidyMenuTier()
     *
     * @param categories 分类列表
     * @param pid        父ID
     * @return 树形列表
     */
    private List<Map<String, Object>> tidyMenuTier(List<SystemAttachmentCategoryEntity> categories, Integer pid) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (SystemAttachmentCategoryEntity category : categories) {
            if (category.getPid().equals(pid)) {
                Map<String, Object> item = categoryEntityToMap(category);

                // PHP: $menu['title'] = $menu['name']
                item.put("title", category.getName());

                // 递归查找子分类
                List<Map<String, Object>> children = tidyMenuTier(categories, category.getId());
                item.put("children", children);

                // PHP: if ($menu['children']) $menu['expand'] = true
                if (!children.isEmpty()) {
                    item.put("expand", true);
                }

                result.add(item);
            }
        }

        return result;
    }

    /**
     * 创建分类
     * PHP Reference: SystemAttachmentCategoryServices::save()
     *
     * 业务逻辑:
     * 1. 检查分类名称是否已存在
     * 2. 保存分类
     *
     * @param data 分类数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(Map<String, Object> data) {
        String name = data.get("name").toString().trim();
        Integer pid = data.containsKey("pid") ? Integer.parseInt(data.get("pid").toString()) : 0;

        // 1. 检查名称是否已存在（PHP: getOne(['name' => $data['name']])）
        QueryWrapper<SystemAttachmentCategoryEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("name", name);
        SystemAttachmentCategoryEntity existing = categoryMapper.selectOne(checkWrapper);

        if (existing != null) {
            throw new CrmChatException("This category already exists");
        }

        // 2. 创建分类
        SystemAttachmentCategoryEntity category = new SystemAttachmentCategoryEntity();
        category.setPid(pid);
        category.setName(name);
        category.setEnname(data.containsKey("enname") ? data.get("enname").toString() : "");

        int result = categoryMapper.insert(category);
        if (result <= 0) {
            throw new CrmChatException("Failed to create");
        }
    }

    /**
     * 获取分类详情
     * PHP Reference: SystemAttachmentCategoryServices::get()
     *
     * @param id 分类ID
     * @return 分类详情
     */
    public Map<String, Object> getCategoryDetail(Integer id) {
        SystemAttachmentCategoryEntity category = categoryMapper.selectById(id);
        if (category == null) {
            throw new CrmChatException("Category does not exist");
        }

        return categoryEntityToMap(category);
    }

    /**
     * 更新分类
     * PHP Reference: SystemAttachmentCategoryServices::update()
     *
     * 业务逻辑:
     * 1. 检查分类名称是否已被其他分类占用
     * 2. 检查是否有子分类 + pid是否改变（有子分类不能修改上级）
     * 3. 更新分类
     *
     * @param id   分类ID
     * @param data 更新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Integer id, Map<String, Object> data) {
        String name = data.get("name").toString().trim();
        Integer newPid = data.containsKey("pid") ? Integer.parseInt(data.get("pid").toString()) : 0;

        // 1. 检查分类是否存在
        SystemAttachmentCategoryEntity category = categoryMapper.selectById(id);
        if (category == null) {
            throw new CrmChatException("Category does not exist");
        }

        // 2. 检查名称是否被其他分类占用（PHP逻辑）
        QueryWrapper<SystemAttachmentCategoryEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("name", name);
        SystemAttachmentCategoryEntity existing = categoryMapper.selectOne(checkWrapper);

        if (existing != null && !existing.getId().equals(id)) {
            throw new CrmChatException("This category already exists");
        }

        // 3. PHP: 如果有子分类且pid改变，不能修改上级
        QueryWrapper<SystemAttachmentCategoryEntity> childrenWrapper = new QueryWrapper<>();
        childrenWrapper.eq("pid", id);
        Long childCount = categoryMapper.selectCount(childrenWrapper);

        if (childCount > 0 && !category.getPid().equals(newPid)) {
            throw new CrmChatException("This category has subcategories, cannot modify parent");
        }

        // 4. 更新分类
        category.setPid(newPid);
        category.setName(name);
        if (data.containsKey("enname")) {
            category.setEnname(data.get("enname").toString());
        }

        int result = categoryMapper.updateById(category);
        if (result <= 0) {
            throw new CrmChatException("Failed to edit");
        }
    }

    /**
     * 删除分类
     * PHP Reference: SystemAttachmentCategoryServices::del()
     *
     * 业务逻辑:
     * 1. 检查是否有子分类
     * 2. 有子分类不能删除
     *
     * @param id 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer id) {
        // 1. 检查是否有子分类（PHP: getCount(['pid' => $id])）
        QueryWrapper<SystemAttachmentCategoryEntity> childrenWrapper = new QueryWrapper<>();
        childrenWrapper.eq("pid", id);
        Long childCount = categoryMapper.selectCount(childrenWrapper);

        if (childCount > 0) {
            throw new CrmChatException("Please delete subcategories first");
        }

        // 2. 删除分类
        int result = categoryMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete");
        }
    }

    /**
     * 分类实体转Map
     */
    private Map<String, Object> categoryEntityToMap(SystemAttachmentCategoryEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("pid", entity.getPid());
        map.put("name", entity.getName());
        map.put("enname", entity.getEnname());
        return map;
    }
}
