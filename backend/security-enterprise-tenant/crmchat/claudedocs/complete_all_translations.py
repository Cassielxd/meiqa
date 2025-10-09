#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
from pathlib import Path

def complete_all_translations():
    """Add translations for all remaining messages"""

    additional_translations = {
        # Error messages with colons (dynamic content)
        "日期格式错误: ": "Invalid date format: ",
        "文件上传失败: ": "File upload failed: ",
        "不支持的文件格式: ": "Unsupported file format: ",
        "只支持图片格式: ": "Only image formats supported: ",

        # Without colons
        "日期格式错误:": "Invalid date format:",
        "文件上传失败:": "File upload failed:",
        "不支持的文件格式:": "Unsupported file format:",
        "只支持图片格式:": "Only image formats supported:",

        # Data operations
        "数据已存在，请勿重复操作": "Data already exists, please do not repeat the operation",

        # Category operations
        "分类编辑成功!": "Category edited successfully",
        "请指定分类": "Please specify category",
        "请输入分类名称": "Please enter category name",
        "请指定目标分类": "Please specify target category",
        "添加配置分类成功!": "Configuration category added successfully",

        # File operations
        "请选择要删除的文件": "Please select files to delete",
        "文件名称不能为空": "File name cannot be empty",
        "请选择要移动的图片": "Please select images to move",
        "移动成功": "Moved successfully",
        "请选择要删除的图片": "Please select images to delete",

        # Account/Login
        "账号不能为空": "Account cannot be empty",
        "密码不能为空": "Password cannot be empty",
        "请输入邮箱地址": "Please enter email address",
        "请填写旧密码": "Please enter old password",
        "请填写新密码": "Please enter new password",
        "请填写确认密码": "Please enter password confirmation",
        "请输入新密码": "Please enter new password",
        "请输入确认密码": "Please enter password confirmation",
        "旧密码不能为空": "Old password cannot be empty",
        "新密码不能为空": "New password cannot be empty",
        "密码重置成功": "Password reset successfully",
        "密码已重置为: admin123": "Password has been reset to: admin123",

        # Update/Modify operations
        "修改成功!": "Updated successfully",
        "修改失败!": "Update failed",

        # Status operations
        "请提供状态值": "Please provide status value",
        "请提供状态参数": "Please provide status parameter",
        "状态值无效": "Invalid status value",
        "状态参数错误，必须是 0 或 1": "Invalid status parameter, must be 0 or 1",
        "状态更新成功": "Status updated successfully",
        "状态参数格式错误": "Invalid status parameter format",

        # Tenant operations
        "无法获取租户信息，请重新登录": "Unable to retrieve tenant information, please log in again",

        # Customer service operations
        "客服添加成功": "Customer service representative added successfully",

        # Date/Time
        "月份错误": "Invalid month",
        "年份错误": "Invalid year",

        # Quick reply/speechcraft operations
        "创建话术成功": "Quick reply created successfully",
        "创建话术失败": "Failed to create quick reply",

        # Administrator operations
        "请填写管理员账号": "Please enter administrator account",
        "请填写管理员密码": "Please enter administrator password",
        "请输入管理员姓名": "Please enter administrator name",
        "请选择管理员身份": "Please select administrator role",
        "管理员ID无效": "Invalid administrator ID",
        "管理员姓名不能为空": "Administrator name cannot be empty",

        # Configuration operations
        "添加配置成功!": "Configuration added successfully",
        "添加身份成功!": "Role added successfully",
        "max_services已更新为5": "max_services has been updated to 5",

        # General operations
        "添加失败": "Failed to add",
        "创建成功": "Created successfully",

        # Authentication
        "未登录或登录已过期": "Not logged in or session expired",

        # Validation
        "key必须存在": "Key must exist",
    }

    # Load existing translation map
    map_file = Path('/Volumes/ORICO/project/kefu/claudedocs/complete_translation_map.json')
    with open(map_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    translation_map = data['translation_map']

    # Update with additional translations
    updated_count = 0
    for chinese, english in additional_translations.items():
        if chinese in translation_map:
            if translation_map[chinese]['english'].startswith('[NEEDS TRANSLATION]'):
                translation_map[chinese]['english'] = english
                updated_count += 1

    # Count remaining untranslated
    untranslated = []
    for chinese, info in translation_map.items():
        if info['english'].startswith('[NEEDS TRANSLATION]'):
            untranslated.append(chinese)

    # Save updated translation map
    output_data = {
        'total_messages': len(translation_map),
        'translated': len(translation_map) - len(untranslated),
        'untranslated': len(untranslated),
        'translation_map': translation_map,
        'untranslated_list': untranslated
    }

    with open(map_file, 'w', encoding='utf-8') as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"Translation map updated successfully")
    print(f"Updated: {updated_count} messages")
    print(f"Total messages: {len(translation_map)}")
    print(f"Fully translated: {len(translation_map) - len(untranslated)}")
    print(f"Still needs translation: {len(untranslated)}")

    if untranslated:
        print("\n=== Still untranslated ===")
        for msg in untranslated:
            print(f"  - {msg}")

    return map_file

if __name__ == '__main__':
    complete_all_translations()
