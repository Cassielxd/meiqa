#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
from pathlib import Path

def fix_file(file_path, replacements):
    """Fix remaining Chinese messages with concatenation"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            original = content

        for chinese, english in replacements:
            # Escape special regex characters in Chinese text
            chinese_escaped = re.escape(chinese)
            # Replace with English
            content = content.replace(chinese, english)

        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

# Define manual replacements for concatenated messages
fixes = {
    '/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/service/AdminTenantService.java': [
        ('"日期格式错误: "', '"Invalid date format: "'),
    ],
    '/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/service/AdminFileService.java': [
        ('"文件上传失败: "', '"File upload failed: "'),
    ],
    '/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/service/storage/LocalFileStorage.java': [
        ('"文件上传失败: "', '"File upload failed: "'),
    ],
    '/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/service/common/ValidationService.java': [
        ('"请输入完整且合法的管理员信息 (账号、密码、姓名)", code = 400', '"Please enter complete and valid administrator information (account, password, name)", code = 400'),
        ('"管理员账号不能为空", code = 400', '"Administrator account cannot be empty", code = 400'),
        ('"管理员密码不能为空", code = 400', '"Administrator password cannot be empty", code = 400'),
        ('"管理员姓名不能为空", code = 400', '"Administrator name cannot be empty", code = 400'),
        ('"请提供管理员账号 (adminAccount)", code = 400', '"Please provide administrator account (adminAccount)", code = 400'),
        ('"请提供管理员密码 (password)", code = 400', '"Please provide administrator password (password)", code = 400'),
        ('"请提供管理员姓名 (adminName)", code = 400', '"Please provide administrator name (adminName)", code = 400'),
        ('"请提供身份ID (identityId)", code = 400', '"Please provide role ID (identityId)", code = 400'),
        ('"管理员账号不能为空 (adminAccount)", code = 400', '"Administrator account cannot be empty (adminAccount)", code = 400'),
        ('"管理员密码不能为空 (password)", code = 400', '"Administrator password cannot be empty (password)", code = 400'),
    ],
    '/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/service/common/FileService.java': [
        ('"文件上传失败: "', '"File upload failed: "'),
        ('"不支持的文件格式: "', '"Unsupported file format: "'),
        ('"只支持图片格式: "', '"Only image formats supported: "'),
    ],
}

# Apply fixes
print("Fixing remaining Chinese messages with concatenation...")
total_fixed = 0

for file_path, replacements in fixes.items():
    print(f"\nProcessing: {Path(file_path).name}")
    if fix_file(file_path, replacements):
        print(f"  ✓ Fixed {len(replacements)} messages")
        total_fixed += 1
    else:
        print(f"  - No changes needed")

print(f"\nTotal files fixed: {total_fixed}")
print("Done!")
