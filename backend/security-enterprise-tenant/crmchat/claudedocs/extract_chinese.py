#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import json
from pathlib import Path

def contains_chinese(text):
    """Check if text contains Chinese characters"""
    return bool(re.search(r'[\u4e00-\u9fff]', text))

def extract_chinese_messages(root_dir):
    """Extract all Chinese messages from Java files"""
    messages = {}
    file_stats = {}

    # Patterns to match
    patterns = [
        # throw new CrmChatException("中文...")
        (r'throw\s+new\s+CrmChatException\("([^"]*)"', 'exception'),
        # ApiResult.ok("中文", ...)
        (r'ApiResult\.ok\("([^"]*)"', 'api_ok'),
        # ApiResult.fail("中文")
        (r'ApiResult\.fail\("([^"]*)"', 'api_fail'),
        # return ApiResult with messages
        (r'return\s+ApiResult\.ok\("([^"]*)"', 'return_ok'),
        (r'return\s+ApiResult\.fail\("([^"]*)"', 'return_fail'),
    ]

    for java_file in Path(root_dir).rglob('*.java'):
        try:
            with open(java_file, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')

                file_messages = []
                for line_num, line in enumerate(lines, 1):
                    for pattern, msg_type in patterns:
                        matches = re.finditer(pattern, line)
                        for match in matches:
                            msg = match.group(1)
                            if contains_chinese(msg):
                                key = msg.strip()
                                if key not in messages:
                                    messages[key] = {
                                        'chinese': key,
                                        'english': '',
                                        'type': msg_type,
                                        'files': []
                                    }

                                file_info = f"{java_file}:{line_num}"
                                if file_info not in messages[key]['files']:
                                    messages[key]['files'].append(file_info)
                                    file_messages.append(key)

                if file_messages:
                    rel_path = str(java_file.relative_to(root_dir))
                    file_stats[rel_path] = len(file_messages)

        except Exception as e:
            print(f"Error processing {java_file}: {e}")

    return messages, file_stats

def main():
    root_dir = Path('/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src')

    print("Extracting Chinese messages from Java files...")
    messages, file_stats = extract_chinese_messages(root_dir)

    print(f"\nFound {len(messages)} unique Chinese messages")
    print(f"In {len(file_stats)} files")

    # Save raw extraction
    output_file = Path('/Volumes/ORICO/project/kefu/claudedocs/chinese_messages_raw.json')
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump({
            'messages': messages,
            'file_stats': file_stats,
            'total_unique_messages': len(messages),
            'total_files': len(file_stats)
        }, f, ensure_ascii=False, indent=2)

    print(f"\nRaw extraction saved to: {output_file}")

    # Print summary
    print("\n=== Top 10 files by message count ===")
    sorted_files = sorted(file_stats.items(), key=lambda x: x[1], reverse=True)[:10]
    for filepath, count in sorted_files:
        print(f"{count:3d} messages: {filepath}")

    # Print sample messages
    print("\n=== Sample messages (first 20) ===")
    for i, (key, data) in enumerate(list(messages.items())[:20], 1):
        print(f"{i}. {key}")
        print(f"   Type: {data['type']}, Files: {len(data['files'])}")

if __name__ == '__main__':
    main()
