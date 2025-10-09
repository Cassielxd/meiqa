#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
from pathlib import Path

def create_translations():
    """Create professional English translations for all Chinese messages"""

    translations = {
        # Label/Tag related
        "缺少标签ID参数": "Missing label ID parameter",
        "标签不存在": "Label does not exist",
        "请先取消关联此标签的用户": "Please remove users associated with this label first",
        "标签名称必须填写": "Label name is required",
        "请选择标签分类": "Please select a label category",
        "缺少标签id": "Missing label ID",

        # Configuration related
        "请输入配置名称": "Please enter configuration name",
        "请输入字段名称": "Please enter field name",
        "请输入配置简介": "Please enter configuration description",
        "请输入文本框的宽度": "Please enter textbox width",
        "请输入正确的文本框的宽度": "Please enter a valid textbox width",
        "请输入多行文本框的宽度": "Please enter textarea width",
        "请输入多行文本框的高度": "Please enter textarea height",
        "请输入正确的多行文本框的宽度": "Please enter a valid textarea width",
        "请输入配置参数": "Please enter configuration parameters",

        # User feedback
        "姓名不能为空": "Name cannot be empty",
        "联系方式不能为空": "Contact information cannot be empty",
        "反馈内容不能为空": "Feedback content cannot be empty",

        # Tenant related
        "租户状态异常：": "Abnormal tenant status: ",
        "租户不存在": "Tenant does not exist",
        "请输入管理员账号": "Please enter administrator account",
        "日期格式错误: ": "Invalid date format: ",

        # Customer service (kefu) related
        "客服不存在或未启用": "Customer service representative does not exist or is not enabled",
        "缺少客服ID": "Missing customer service ID",
        "当前客服不存在": "Current customer service representative does not exist",
        "登录的客服不存在": "Logged-in customer service representative does not exist",
        "请先填写客服账号和密码再尝试进入客服平台": "Please enter customer service account and password before accessing the platform",
        "客服帐号已被禁用": "Customer service account has been disabled",
        "该客服账号已存在!": "This customer service account already exists",
        "客服名称不能为空！": "Customer service name cannot be empty",
        "账号必须为数字或者字母的组合4-30位": "Account must be 4-30 alphanumeric characters",
        "该客服账号已存在!": "This customer service account already exists",
        "请选择客服头像": "Please select customer service avatar",
        "密码必须为数字或者字母的组合6-20位": "Password must be 6-20 alphanumeric characters",
        "两次输入的密码不正确": "Passwords do not match",
        "修改失败,请稍候再试!": "Update failed, please try again later",
        "数据不存在": "Data does not exist",
        "删除失败,请稍候再试!": "Delete failed, please try again later",
        "状态更新失败": "Status update failed",
        "数据不存在!": "Data does not exist",
        "客服添加失败，请稍后再试": "Failed to add customer service representative, please try again later",
        "未配置可用应用，请先创建应用": "No available application configured, please create an application first",
        "请输入正确的手机号": "Please enter a valid phone number",
        "请输入账号": "Please enter account",
        "请输入密码": "Please enter password",
        "请输入客服昵称": "Please enter customer service nickname",
        "该手机号的客服已存在!": "Customer service representative with this phone number already exists",
        "客服ID不能为空": "Customer service ID cannot be empty",

        # File related
        "文件上传失败: ": "File upload failed: ",
        "文件存储服务未配置": "File storage service not configured",
        "文件名不合法": "Invalid file name",
        "不支持的文件格式: ": "Unsupported file format: ",
        "只支持图片格式: ": "Only image formats supported: ",

        # Admin related
        "管理员已被删除": "Administrator has been deleted",
        "管理员信息读取失败": "Failed to read administrator information",

        # Generic operations
        "无权访问该资源": "No permission to access this resource",
        "缺少参数": "Missing parameters",
        "参数错误": "Invalid parameters",
        "参数错误：value必须为0或1": "Invalid parameter: value must be 0 or 1",

        # Success messages
        "添加成功": "Added successfully",
        "保存成功": "Saved successfully",
        "修改成功": "Updated successfully",
        "删除成功": "Deleted successfully",
        "删除成功!": "Deleted successfully",
        "删除成功！": "Deleted successfully",
        "更新成功": "Updated successfully",
        "设置成功": "Set successfully",
        "提交成功！": "Submitted successfully",
        "编辑成功": "Edited successfully",
        "重置成功": "Reset successfully",
        "登出成功": "Logged out successfully",
        "退出成功": "Logged out successfully",
        "发送成功": "Sent successfully",
        "转接成功": "Transferred successfully",
        "拉黑成功": "Blocked successfully",
        "投诉成功": "Complaint submitted successfully",
        "批量设置成功": "Batch setting successful",
        "密码修改成功": "Password changed successfully",
        "查询成功": "Query successful",
        "图片上传成功!": "Image uploaded successfully",

        # Failure messages
        "修改失败": "Update failed",
        "删除失败": "Delete failed",

        # User/client related
        "缺少client_id参数": "Missing client_id parameter",
        "缺少用户ID参数": "Missing user ID parameter",
        "缺少用户ID": "Missing user ID",
        "至少选择一个用户": "Please select at least one user",

        # Authentication/Password
        "请填写完整信息": "Please fill in complete information",
        "两次输入的新密码不一致": "New passwords do not match",
        "新密码长度不能少于6位": "New password must be at least 6 characters",
        "设置的密码过于简单(不小于六位包含数字字母)": "Password is too simple (must be at least 6 characters containing letters and numbers)",
        "请先登录": "Please log in first",
        "登录CODE不存在": "Login code does not exist",

        # Transfer/routing related
        "缺少目标客服ID": "Missing target customer service ID",

        # Application related
        "请从token中获取租户ID并实现更新逻辑": "Please get tenant ID from token and implement update logic",

        # Group/Category related
        "请选择分组": "Please select a group",

        # File upload
        "请选择文件": "Please select a file",
        "请选择上传文件": "Please select a file to upload",
        "请选择要上传的文件": "Please select a file to upload",
        "上传的文件不能为空": "Upload file cannot be empty",
        "文件为空": "File is empty",
        "请输入存储路径": "Please enter storage path",
        "存储路径不正确": "Invalid storage path",
        "文件存储失败": "File storage failed",
        "上传成功": "Uploaded successfully",
        "上传失败": "Upload failed",

        # Message/Chat related
        "消息发送失败": "Message sending failed",
        "发送失败": "Sending failed",

        # Validation
        "请输入正确的": "Please enter a valid ",
        "不能为空": " cannot be empty",
        "必须填写": " is required",

        # Settings
        "保存设置成功": "Settings saved successfully",
        "保存设置失败": "Failed to save settings",
        "获取设置失败": "Failed to get settings",

        # Status messages
        "success": "success",

        # Speech craft (quick replies)
        "话术": "Quick reply",
        "话术分类": "Quick reply category",
        "话术内容": "Quick reply content",

        # Complex messages with variables
        "文件大小超过限制": "File size exceeds limit",
        "文件类型不支持": "File type not supported",

        # System messages
        "系统繁忙，请稍后再试": "System is busy, please try again later",
        "操作失败": "Operation failed",
        "操作成功": "Operation successful",
    }

    return translations

def merge_with_extracted():
    """Merge manual translations with extracted messages"""

    # Load extracted messages
    raw_file = Path('/Volumes/ORICO/project/kefu/claudedocs/chinese_messages_raw.json')
    with open(raw_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    messages = data['messages']
    manual_translations = create_translations()

    # Create complete translation map
    translation_map = {}
    untranslated = []

    for chinese, info in messages.items():
        if chinese in manual_translations:
            translation_map[chinese] = {
                'chinese': chinese,
                'english': manual_translations[chinese],
                'type': info['type'],
                'file_count': len(info['files']),
                'files': info['files']
            }
        else:
            # Mark as needing translation
            untranslated.append(chinese)
            translation_map[chinese] = {
                'chinese': chinese,
                'english': f"[NEEDS TRANSLATION] {chinese}",
                'type': info['type'],
                'file_count': len(info['files']),
                'files': info['files']
            }

    # Save complete translation map
    output_file = Path('/Volumes/ORICO/project/kefu/claudedocs/complete_translation_map.json')
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump({
            'total_messages': len(translation_map),
            'translated': len(translation_map) - len(untranslated),
            'untranslated': len(untranslated),
            'translation_map': translation_map,
            'untranslated_list': untranslated
        }, f, ensure_ascii=False, indent=2)

    print(f"Complete translation map created: {output_file}")
    print(f"Total messages: {len(translation_map)}")
    print(f"Translated: {len(translation_map) - len(untranslated)}")
    print(f"Needs translation: {len(untranslated)}")

    if untranslated:
        print("\n=== Messages needing translation ===")
        for msg in untranslated[:10]:
            print(f"  - {msg}")
        if len(untranslated) > 10:
            print(f"  ... and {len(untranslated) - 10} more")

    return output_file

if __name__ == '__main__':
    merge_with_extracted()
