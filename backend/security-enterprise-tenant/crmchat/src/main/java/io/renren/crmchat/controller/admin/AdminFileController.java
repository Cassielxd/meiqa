package io.renren.crmchat.controller.admin;

import io.renren.common.page.PageData;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminFileService;
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
 * Admin API - 附件管理
 * PHP Reference: /app/controller/admin/file/Attachment.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/file")
@Tag(name = "Admin - Attachment Management")
@AllArgsConstructor
public class AdminFileController {

    private final AdminFileService adminFileService;

    /**
     * 7.1 图片附件列表
     * GET /api/admin/file/file
     *
     * PHP Reference: Attachment.php::index()
     *
     * Query Params:
     * - page: 页码
     * - limit: 每页数量
     * - pid: 分类ID（默认0表示全部）
     * - name: 附件名称（搜索）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping("/file")
    @Operation(summary = "Image Attachment List")
    public ApiResult<PageData<Map<String, Object>>> fileList(@RequestParam Map<String, Object> params) {
        PageData<Map<String, Object>> result = adminFileService.getFileList(params);
        return ApiResult.ok(result);
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload Image (Default Type)")
    public ApiResult<Map<String, Object>> uploadDefault(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "pid", required = false, defaultValue = "0") Integer pid) {

        return uploadWithType(0, file, pid);
    }

    /**
     * 上传图片（带 upload_type 参数）
     * POST /api/tenant/file/upload/:upload_type
     *
     * PHP Reference: Attachment.php::upload()
     *
     * Form Data:
     * - file: 文件字段（必填）
     * - pid: 分类ID（可选，默认0）
     *
     * Response:
     * {
     *   "src": "/uploads/20250101/image.jpg"
     * }
     */
    @PostMapping("/upload/{upload_type}")
    @Operation(summary = "Upload Image (Specific Type)")
    public ApiResult<Map<String, Object>> uploadWithType(
            @Parameter(description = "Upload Type: 0-Auto, 1-Local, 2-OSS") @PathVariable("upload_type") Integer uploadType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "pid", required = false, defaultValue = "0") Integer pid) {

        if (file == null || file.isEmpty()) {
            return ApiResult.fail("Please select a file to upload");
        }

        if (uploadType == null) {
            uploadType = 0; // 默认自动选择
        }

        // PHP: $res = $this->service->upload((int)$pid, $file, $upload_type, $type);
        // PHP: return $this->success('上传成功', ['src' => $res]);
        Map<String, Object> result = adminFileService.uploadFile(file, pid, uploadType);

        // PHP 只返回 src 字段
        Map<String, Object> response = new HashMap<>();
        response.put("src", result.get("att_dir")); // PHP 返回的是文件路径

        return ApiResult.ok("Uploaded successfully", response);
    }

    /**
     * 7.3 删除图片
     * POST /api/admin/file/file/delete
     *
     * PHP Reference: Attachment.php::delete()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3]  // or comma-separated string "1,2,3"
     * }
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @PostMapping("/file/delete")
    @Operation(summary = "Delete Image")
    public ApiResult<String> delete(@RequestBody Map<String, Object> data) {
        if (!data.containsKey("ids")) {
            return ApiResult.fail("Please select images to delete");
        }

        Object idsObj = data.get("ids");
        adminFileService.deleteFiles(idsObj);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 7.4 修改图片名称
     * PUT /api/admin/file/file/update/:id
     *
     * PHP Reference: Attachment.php::update($id)
     *
     * Request Body:
     * {
     *   "name": "New Image Name"  // or use "real_name"
     * }
     *
     * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping("/file/update/{id}")
    @Operation(summary = "Rename Image")
    public ApiResult<String> update(
            @Parameter(description = "Attachment ID") @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        // PHP uses "real_name", but API schema uses "name"
        String newName = null;
        if (data.containsKey("name")) {
            newName = (String) data.get("name");
        } else if (data.containsKey("real_name")) {
            newName = (String) data.get("real_name");
        }

        if (newName == null || newName.trim().isEmpty()) {
            return ApiResult.fail("File name cannot be empty");
        }

        adminFileService.updateFileName(id, newName);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 7.5 移动图片分类
     * PUT /api/admin/file/file/do_move
     *
     * PHP Reference: Attachment.php::moveImageCate()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],  // or from "images" field
     *   "pid": 2
     * }
     *
     * Response: { "code": 0, "msg": "Moved successfully" }
     */
    @PutMapping("/file/do_move")
    @Operation(summary = "Move Image Category")
    public ApiResult<String> moveFiles(@RequestBody Map<String, Object> data) {
        if (!data.containsKey("pid")) {
            return ApiResult.fail("Please specify target category");
        }

        // PHP uses "images" field, but API schema uses "ids"
        Object idsObj = null;
        if (data.containsKey("ids")) {
            idsObj = data.get("ids");
        } else if (data.containsKey("images")) {
            idsObj = data.get("images");
        }

        if (idsObj == null) {
            return ApiResult.fail("Please select images to move");
        }

        int targetPid = Integer.parseInt(data.get("pid").toString());
        adminFileService.moveFiles(idsObj, targetPid);
        return ApiResult.ok("Moved successfully", "success");
    }

    // ==================== 附件分类管理 ====================

    /**
     * 7.6.1 获取分类列表
     * GET /api/admin/file/category
     *
     * PHP Reference: AttachmentCategory.php::index()
     *
     * Query Params:
     * - name: 分类名称搜索（可选）
     *
     * Response:
     * {
     *   "list": [树形结构的分类列表]
     * }
     */
    @GetMapping("/category")
    @Operation(summary = "Get Category List")
    public ApiResult<Map<String, Object>> categoryList(@RequestParam(required = false) Map<String, Object> params) {
        Map<String, Object> result = adminFileService.getCategoryList(params);
        return ApiResult.ok(result);
    }

    /**
     * 7.6.2 创建分类
     * POST /api/admin/file/category
     *
     * PHP Reference: AttachmentCategory.php::save()
     */
    @PostMapping("/category")
    @Operation(summary = "Create Category")
    public ApiResult<String> createCategory(@RequestBody Map<String, Object> data) {
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter category name");
        }

        adminFileService.createCategory(data);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 7.6.3 获取分类详情
     * GET /api/admin/file/category/:id
     */
    @GetMapping("/category/{id}")
    @Operation(summary = "Get Category Detail")
    public ApiResult<Map<String, Object>> categoryDetail(@PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        Map<String, Object> category = adminFileService.getCategoryDetail(id);
        return ApiResult.ok(category);
    }

    /**
     * 7.6.4 更新分类
     * PUT /api/admin/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::update()
     */
    @PutMapping("/category/{id}")
    @Operation(summary = "Update Category")
    public ApiResult<String> updateCategory(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter category name");
        }

        adminFileService.updateCategory(id, data);
        return ApiResult.ok("Category edited successfully", "success");
    }

    /**
     * 7.6.5 删除分类
     * DELETE /api/admin/file/category/:id
     *
     * PHP Reference: AttachmentCategory.php::delete()
     */
    @DeleteMapping("/category/{id}")
    @Operation(summary = "Delete Category")
    public ApiResult<String> deleteCategory(@PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        adminFileService.deleteCategory(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
