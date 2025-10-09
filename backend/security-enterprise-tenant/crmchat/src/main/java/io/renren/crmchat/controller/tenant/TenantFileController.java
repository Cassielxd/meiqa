package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.SystemAttachmentCategoryEntity;
import io.renren.crmchat.entity.SystemAttachmentEntity;
import io.renren.crmchat.service.TenantFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant API - 文件管理
 * PHP Reference: /app/controller/tenant/file/AttachmentCategory.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant/file")
@Tag(name = "Tenant - File Management")
@AllArgsConstructor
public class TenantFileController {

    private final TenantFileService tenantFileService;

    /**
     * 获取附件分类列表
     * GET /api/tenant/file/category
     *
     * PHP Reference: AttachmentCategory.php::index()
     *
     * Query Parameters:
     * - name: 分类名称（可选，模糊搜索）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "pid": 0,
     *     "name": "Image Category",
     *     "enname": "image"
     *   }
     * ]
     */
    @GetMapping("/category")
    @Operation(summary = "Get Attachment Category List")
    public ApiResult<List<SystemAttachmentCategoryEntity>> categoryList(
            @RequestParam(required = false) String name) {

        // 构建过滤条件
        Map<String, Object> filters = new HashMap<>();
        if (name != null) {
            filters.put("name", name);
        }

        List<SystemAttachmentCategoryEntity> list = tenantFileService.getFileCategoryList(filters);
        return ApiResult.ok(list);
    }

    /**
     * 获取附件分类详情
     * GET /api/tenant/file/category/:id
     *
     * PHP Reference: Resource route - read()
     *
     * Response:
     * {
     *   "id": 1,
     *   "pid": 0,
     *   "name": "图片分类",
     *   "enname": "image"
     * }
     */
    @GetMapping("/category/{id}")
    @Operation(summary = "Get Attachment Category Details")
    public ApiResult<SystemAttachmentCategoryEntity> categoryDetail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        SystemAttachmentCategoryEntity category = tenantFileService.getFileCategoryDetail(id);
        return ApiResult.ok(category);
    }

    /**
     * 创建附件分类
     * POST /api/tenant/file/category
     *
     * PHP Reference: AttachmentCategory.php::save()
     *
     * Request Body:
     * {
     *   "name": "图片分类",    // 必填
     *   "pid": 0             // 可选，默认0（顶级分类）
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping("/category")
    @Operation(summary = "创建附件分类")
    public ApiResult<String> createCategory(@RequestBody Map<String, Object> data) {

        tenantFileService.createFileCategory(data);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 更新附件分类
     * PUT /api/tenant/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::update()
     *
     * Request Body:
     * {
     *   "name": "图片分类",    // 必填
     *   "pid": 0             // 可选
     * }
     *
     * 注意：如果分类有下级分类，不能修改pid
     *
     * Response: { "code": 0, "msg": "分类编辑成功!" }
     */
    @PutMapping("/category/{id}")
    @Operation(summary = "Update Attachment Category")
    public ApiResult<String> updateCategory(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantFileService.updateFileCategory(id, data);
        return ApiResult.ok("Category edited successfully", "success");
    }

    /**
     * 删除附件分类
     * DELETE /api/tenant/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功!" }
     */
    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除附件分类")
    public ApiResult<String> deleteCategory(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantFileService.deleteFileCategory(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 获取图片附件列表
     * GET /api/tenant/file/file
     *
     * PHP Reference: Attachment.php::index()
     *
     * Query Parameters:
     * - pid: 分类ID（可选，默认0）
     *
     * Response:
     * [
     *   {
     *     "att_id": 1,
     *     "name": "image.jpg",
     *     "real_name": "image.jpg",
     *     "att_dir": "/uploads/image.jpg",
     *     "satt_dir": "/uploads/thumb_image.jpg",
     *     "att_size": "102400",
     *     "att_type": "image/jpeg",
     *     "image_type": 1,
     *     "pid": 0,
     *     "time": 1672531200
     *   }
     * ]
     */
    @GetMapping("/file")
    @Operation(summary = "获取图片附件列表")
    public ApiResult<List<SystemAttachmentEntity>> fileList(
            @RequestParam(required = false) Integer pid) {

        // 构建过滤条件
        Map<String, Object> filters = new HashMap<>();
        if (pid != null) {
            filters.put("pid", pid);
        }

        List<SystemAttachmentEntity> list = tenantFileService.getFileList(filters);
        return ApiResult.ok(list);
    }

    /**
     * 上传图片
     * POST /api/tenant/file/upload/:upload_type?
     *
     * PHP Reference: Attachment.php::upload()
     *
     * Form Data:
     * - file: 文件字段（必填）
     * - pid: 分类ID（可选，默认0）
     *
     * Response:
     * {
     *   "src": "/uploads/20250101/image.jpg",
     *   "att_id": 1,
     *   "name": "image.jpg",
     *   "att_dir": "/uploads/20250101/image.jpg",
     *   "satt_dir": "/uploads/20250101/thumb_image.jpg",
     *   "att_size": "102400",
     *   "att_type": "image/jpeg",
     *   "image_type": 1
     * }
     */
    @PostMapping("/upload/{upload_type}")
    @Operation(summary = "Upload Image")
    public ApiResult<Map<String, Object>> upload(
            @Parameter(description = "Upload Type: 0-Auto, 1-Local, 2-OSS") @PathVariable(value = "upload_type", required = false) Integer uploadType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "pid", required = false) Integer pid) {

        if (file == null || file.isEmpty()) {
            return ApiResult.fail("Please select a file to upload");
        }

        if (uploadType == null) {
            uploadType = 0; // 默认自动选择
        }

        if (pid == null) {
            pid = 0; // 默认分类
        }

        Map<String, Object> result = tenantFileService.uploadFile(file, pid, uploadType);
        return ApiResult.ok("Uploaded successfully", result);
    }

    /**
     * 删除图片
     * POST /api/tenant/file/file/delete
     *
     * PHP Reference: Attachment.php::delete()
     *
     * Request Body:
     * {
     *   "ids": "1,2,3"  // 附件ID列表（逗号分隔字符串）
     * }
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @PostMapping("/file/delete")
    @Operation(summary = "Delete Image")
    public ApiResult<String> deleteFile(@RequestBody Map<String, Object> data) {

        if (!data.containsKey("ids") || data.get("ids") == null) {
            return ApiResult.fail("Please select files to delete");
        }

        String ids = data.get("ids").toString();
        tenantFileService.deleteFiles(ids);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 修改图片名称
     * PUT /api/tenant/file/file/update/:id
     *
     * PHP Reference: Attachment.php::update()
     *
     * Request Body:
     * {
     *   "real_name": "新文件名.jpg"  // 必填
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/file/update/{id}")
    @Operation(summary = "Modify Image Name")
    public ApiResult<String> updateFile(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (!data.containsKey("real_name") || data.get("real_name") == null) {
            return ApiResult.fail("File name cannot be empty");
        }

        String realName = data.get("real_name").toString();
        tenantFileService.updateFileName(id, realName);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 移动图片分类
     * PUT /api/tenant/file/file/do_move
     *
     * PHP Reference: Attachment.php::moveImageCate()
     *
     * Request Body:
     * {
     *   "pid": 1,           // 新的分类ID
     *   "images": "1,2,3"   // 图片ID列表（逗号分隔字符串）
     * }
     *
     * Response: { "code": 0, "msg": "移动成功" }
     */
    @PutMapping("/file/do_move")
    @Operation(summary = "Move Image Category")
    public ApiResult<String> moveFiles(@RequestBody Map<String, Object> data) {

        if (!data.containsKey("pid") || data.get("pid") == null) {
            return ApiResult.fail("Please specify category");
        }

        if (!data.containsKey("images") || data.get("images") == null) {
            return ApiResult.fail("Please select images to move");
        }

        Integer pid = Integer.parseInt(data.get("pid").toString());
        String images = data.get("images").toString();

        tenantFileService.moveFiles(pid, images);
        return ApiResult.ok("Moved successfully", "success");
    }
}
