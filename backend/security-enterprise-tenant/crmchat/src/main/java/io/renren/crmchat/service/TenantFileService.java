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
    private final io.renren.crmchat.formbuilder.FormBuilder formBuilder;

    /**
     * 获取附件分类列表
     * GET /api/tenant/file/category
     *
     * PHP Reference: AttachmentCategory.php::index() -> SystemAttachmentCategoryServices::getAll()
     *
     * 业务逻辑:
     * 1. 接收过滤参数（name）
     * 2. 查询所有分类或按name模糊搜索
     * 3. 转换为树形结构（添加 title、children、expand 字段）
     * 4. 返回分类列表
     *
     * @param filters 过滤条件（name）
     * @return 树形分类列表
     */
    public List<SystemAttachmentCategoryEntity> getFileCategoryList(Map<String, Object> filters) {
        // PHP: $where = $this->request->getMore([['name', '']]);
        // PHP: $categoryList = $this->dao->getList($where);

        QueryWrapper<SystemAttachmentCategoryEntity> wrapper = new QueryWrapper<>();

        // 可选name过滤
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().trim().isEmpty()) {
            wrapper.like("name", filters.get("name").toString());
        }

        List<SystemAttachmentCategoryEntity> categoryList = attachmentCategoryMapper.selectList(wrapper);

        // PHP: $list = $this->tidyMenuTier($categoryList);
        // 转换为树形结构
        return buildCategoryTree(categoryList, 0);
    }

    /**
     * 构建分类树形结构
     * PHP Reference: SystemAttachmentCategoryServices::tidyMenuTier()
     *
     * @param menusList 扁平分类列表
     * @param pid 父级ID
     * @return 树形结构列表
     */
    private List<SystemAttachmentCategoryEntity> buildCategoryTree(List<SystemAttachmentCategoryEntity> menusList, Integer pid) {
        List<SystemAttachmentCategoryEntity> navList = new ArrayList<>();

        for (SystemAttachmentCategoryEntity menu : menusList) {
            // PHP: $menu['title'] = $menu['name'];
            menu.setTitle(menu.getName());

            // PHP: if ($menu['pid'] == $pid)
            if (menu.getPid().equals(pid)) {
                // PHP: $menu['children'] = $this->tidyMenuTier($menusList, $menu['id']);
                List<SystemAttachmentCategoryEntity> children = buildCategoryTree(menusList, menu.getId());
                menu.setChildren(children);

                // PHP: if ($menu['children']) $menu['expand'] = true;
                if (children != null && !children.isEmpty()) {
                    menu.setExpand(true);
                }

                navList.add(menu);
            }
        }

        return navList;
    }

    /**
     * 获取创建附件分类表单配置
     * GET /api/tenant/file/category/create?id=
     *
     * PHP Reference: SystemAttachmentCategoryServices::createForm()
     *
     * @param pid 父级分类ID
     * @return 表单配置
     */
    public Map<String, Object> getFileCategoryCreateForm(Integer pid) {
        // PHP: create_form('添加分类', $this->form(['pid' => $pid]), Url::buildUrl('/file/category'), 'POST');

        // 获取分类选项列表
        List<io.renren.crmchat.formbuilder.components.OptionComponent> categoryOptions = getCategoryOptions();

        List<io.renren.crmchat.formbuilder.components.BaseComponent> fields = new java.util.ArrayList<>();

        // PHP: Form::select('pid', '上级分类', (int)($info['pid'] ?? ''))->setOptions($this->getCateList(['pid' => 0]))->filterable(1)
        fields.add(formBuilder.select("pid", "上级分类", pid != null ? pid : 0)
            .options(categoryOptions)
            .filterable());

        // PHP: Form::input('name', '分类名称', $info['name'] ?? '')->maxlength(30)
        fields.add(formBuilder.input("name", "分类名称", "")
            .maxlength(30));

        return io.renren.crmchat.formbuilder.FormHelper.createForm(
            "添加分类",
            fields,
            "/file/category",
            "POST"
        );
    }

    /**
     * 获取编辑附件分类表单配置
     * GET /api/tenant/file/category/:id/edit
     *
     * PHP Reference: SystemAttachmentCategoryServices::editForm()
     *
     * @param id 分类ID
     * @return 表单配置
     */
    public Map<String, Object> getFileCategoryEditForm(Integer id) {
        // PHP: $info = $this->dao->get($id);
        SystemAttachmentCategoryEntity category = attachmentCategoryMapper.selectById(id);
        if (category == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Category does not exist");
        }

        // 获取分类选项列表
        List<io.renren.crmchat.formbuilder.components.OptionComponent> categoryOptions = getCategoryOptions();

        List<io.renren.crmchat.formbuilder.components.BaseComponent> fields = new java.util.ArrayList<>();

        // PHP: Form::select('pid', '上级分类', (int)($info['pid'] ?? ''))->setOptions($this->getCateList(['pid' => 0]))->filterable(1)
        fields.add(formBuilder.select("pid", "上级分类", category.getPid())
            .options(categoryOptions)
            .filterable());

        // PHP: Form::input('name', '分类名称', $info['name'] ?? '')->maxlength(30)
        fields.add(formBuilder.input("name", "分类名称", category.getName())
            .maxlength(30));

        return io.renren.crmchat.formbuilder.FormHelper.createForm(
            "编辑分类",
            fields,
            "/file/category/" + id,
            "PUT"
        );
    }

    /**
     * 获取分类选项列表（用于下拉选择）
     * PHP: $this->getCateList(['pid' => 0])
     */
    private List<io.renren.crmchat.formbuilder.components.OptionComponent> getCategoryOptions() {
        QueryWrapper<SystemAttachmentCategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", 0); // 只获取顶级分类
        List<SystemAttachmentCategoryEntity> categories = attachmentCategoryMapper.selectList(wrapper);

        List<io.renren.crmchat.formbuilder.components.OptionComponent> options = new java.util.ArrayList<>();
        // 添加默认选项
        options.add(new io.renren.crmchat.formbuilder.components.OptionComponent(0, "顶级分类", false));

        for (SystemAttachmentCategoryEntity category : categories) {
            options.add(new io.renren.crmchat.formbuilder.components.OptionComponent(
                category.getId(),
                category.getName(),
                false
            ));
        }

        return options;
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
     * PHP Reference: Attachment.php::index() -> SystemAttachmentServices::getImageList()
     *
     * 业务逻辑:
     * 1. 接收 pid、page、limit 参数
     * 2. 查询指定分类下的附件（带分页）
     * 3. 添加 module_type = 1 过滤
     * 4. 按 att_id DESC 排序
     * 5. 处理 URL（添加 site_url 前缀）
     * 6. 统计总数
     * 7. 返回 {list, count}
     *
     * @param filters 过滤条件（pid, page, limit）
     * @return {list: 附件列表, count: 总数}
     */
    public Map<String, Object> getFileList(Map<String, Object> filters) {
        // PHP: [$page, $limit] = $this->getPageValue();
        int page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        int limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 18;

        // PHP: $list = $this->dao->getList($where, $page, $limit);
        QueryWrapper<SystemAttachmentEntity> wrapper = new QueryWrapper<>();

        // 过滤条件：pid
        if (filters.containsKey("pid") && filters.get("pid") != null) {
            wrapper.eq("pid", Integer.parseInt(filters.get("pid").toString()));
        } else {
            wrapper.eq("pid", 0); // 默认查询顶级分类
        }

        // PHP: ->where('module_type', 1)->order('att_id DESC')
        wrapper.eq("module_type", 1);
        wrapper.orderByDesc("att_id");

        // 计算分页偏移量
        int offset = (page - 1) * limit;
        wrapper.last("LIMIT " + offset + ", " + limit);

        List<SystemAttachmentEntity> list = systemAttachmentMapper.selectList(wrapper);

        // PHP: $site_url = sys_config('site_url');
        // PHP: foreach ($list as &$item) { ... }
        String siteUrl = getSiteUrl();
        if (siteUrl != null && !siteUrl.isEmpty()) {
            for (SystemAttachmentEntity item : list) {
                // 处理 satt_dir（缩略图路径）
                if (item.getSattDir() != null && !item.getSattDir().isEmpty()) {
                    if (!item.getSattDir().startsWith("http") && !item.getSattDir().contains(siteUrl)) {
                        item.setSattDir(siteUrl + item.getSattDir());
                    }
                }
                // 处理 att_dir（原图路径）
                if (item.getAttDir() != null && !item.getAttDir().isEmpty()) {
                    if (!item.getAttDir().startsWith("http") && !item.getAttDir().contains(siteUrl)) {
                        item.setAttDir(siteUrl + item.getAttDir());
                    }
                }
            }
        }

        // PHP: $where['module_type'] = 1;
        // PHP: $count = $this->dao->count($where);
        QueryWrapper<SystemAttachmentEntity> countWrapper = new QueryWrapper<>();
        if (filters.containsKey("pid") && filters.get("pid") != null) {
            countWrapper.eq("pid", Integer.parseInt(filters.get("pid").toString()));
        } else {
            countWrapper.eq("pid", 0);
        }
        countWrapper.eq("module_type", 1);
        Long count = systemAttachmentMapper.selectCount(countWrapper);

        // PHP: return compact('list', 'count');
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count.intValue()); // 转换为int确保JSON序列化为数字
        return result;
    }

    /**
     * 获取网站 URL 配置
     * PHP: sys_config('site_url')
     */
    private String getSiteUrl() {
        // TODO: 从系统配置获取 site_url
        // 暂时返回空，实际应该从配置表读取
        return "";
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

        // 4. PHP逻辑: 确保URL包含完整域名前缀
        // PHP: path_to_url($res['dir'])
        // PHP: if (strpos($res['dir'], 'http') === false) $res['dir'] = $request->domain() . $res['dir'];
        String completeUrl = filePath;
        if (!filePath.startsWith("http://") && !filePath.startsWith("https://")) {
            completeUrl = fileService.getFileUrl(filePath);
        }

        // 5. 创建附件记录
        SystemAttachmentEntity attachment = new SystemAttachmentEntity();
        attachment.setName(originalFilename);
        attachment.setRealName(originalFilename);
        attachment.setAttDir(completeUrl);
        attachment.setSattDir(completeUrl); // 缩略图路径（与原图相同）
        attachment.setAttSize(String.valueOf(fileSize));
        attachment.setAttType(contentType);
        attachment.setImageType(uploadType);
        attachment.setModuleType(1); // 1-附件模块
        attachment.setTime((int) (System.currentTimeMillis() / 1000));
        attachment.setPid(pid);

        // 6. 保存到数据库
        int result = systemAttachmentMapper.insert(attachment);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save attachment record");
        }

        // 7. 返回附件信息（PHP格式）
        Map<String, Object> response = new HashMap<>();
        response.put("src", attachment.getAttDir()); // PHP返回src字段（完整URL）
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
