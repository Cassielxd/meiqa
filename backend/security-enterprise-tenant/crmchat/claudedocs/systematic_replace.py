#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import re
from pathlib import Path
from datetime import datetime

def load_translation_map():
    """Load the complete translation map"""
    map_file = Path('/Volumes/ORICO/project/kefu/claudedocs/complete_translation_map.json')
    with open(map_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data['translation_map']

def escape_for_regex(text):
    """Escape special regex characters"""
    return re.escape(text)

def replace_in_file(file_path, translation_map):
    """Replace all Chinese messages in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            original_content = content

        replacements_made = []

        # Process each Chinese message
        for chinese, trans_info in translation_map.items():
            english = trans_info['english']

            # Skip if English translation is empty
            if not english or english.startswith('[NEEDS TRANSLATION]'):
                continue

            # Escape for regex
            chinese_escaped = escape_for_regex(chinese)

            # Patterns to match and replace
            patterns = [
                # throw new CrmChatException("中文")
                (rf'(throw\s+new\s+CrmChatException\(")({chinese_escaped})(")',
                 rf'\1{english}\3'),

                # ApiResult.ok("中文", ...)
                (rf'(ApiResult\.ok\(")({chinese_escaped})(")',
                 rf'\1{english}\3'),

                # ApiResult.fail("中文")
                (rf'(ApiResult\.fail\(")({chinese_escaped})(")',
                 rf'\1{english}\3'),

                # return ApiResult.ok("中文", ...)
                (rf'(return\s+ApiResult\.ok\(")({chinese_escaped})(")',
                 rf'\1{english}\3'),

                # return ApiResult.fail("中文")
                (rf'(return\s+ApiResult\.fail\(")({chinese_escaped})(")',
                 rf'\1{english}\3'),
            ]

            for pattern, replacement in patterns:
                if re.search(pattern, content):
                    new_content = re.sub(pattern, replacement, content)
                    if new_content != content:
                        replacements_made.append({
                            'chinese': chinese,
                            'english': english,
                            'pattern_type': pattern[:30]
                        })
                        content = new_content

        # Only write if changes were made
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return len(replacements_made), replacements_made
        else:
            return 0, []

    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return 0, []

def process_all_files():
    """Process all Java files with Chinese messages"""
    translation_map = load_translation_map()

    # Get list of files to process from extraction
    raw_file = Path('/Volumes/ORICO/project/kefu/claudedocs/chinese_messages_raw.json')
    with open(raw_file, 'r', encoding='utf-8') as f:
        raw_data = json.load(f)

    file_stats = raw_data['file_stats']

    # Organize files by type
    service_files = []
    controller_files = []
    other_files = []

    for rel_path in file_stats.keys():
        full_path = Path('/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src') / rel_path
        if '/service/' in str(rel_path):
            service_files.append(full_path)
        elif '/controller/' in str(rel_path):
            controller_files.append(full_path)
        else:
            other_files.append(full_path)

    # Process files
    results = {
        'services': {},
        'controllers': {},
        'others': {}
    }

    print("=" * 80)
    print("PHASE 1: Processing Service Files")
    print("=" * 80)

    for i, file_path in enumerate(service_files, 1):
        print(f"[{i}/{len(service_files)}] Processing {file_path.name}...", end=' ')
        count, replacements = replace_in_file(file_path, translation_map)
        if count > 0:
            print(f"✓ {count} replacements")
            results['services'][str(file_path.relative_to(Path('/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src')))] = {
                'count': count,
                'replacements': replacements
            }
        else:
            print("(no changes)")

    print("\n" + "=" * 80)
    print("PHASE 2: Processing Controller Files")
    print("=" * 80)

    for i, file_path in enumerate(controller_files, 1):
        print(f"[{i}/{len(controller_files)}] Processing {file_path.name}...", end=' ')
        count, replacements = replace_in_file(file_path, translation_map)
        if count > 0:
            print(f"✓ {count} replacements")
            results['controllers'][str(file_path.relative_to(Path('/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src')))] = {
                'count': count,
                'replacements': replacements
            }
        else:
            print("(no changes)")

    print("\n" + "=" * 80)
    print("PHASE 3: Processing Other Files")
    print("=" * 80)

    for i, file_path in enumerate(other_files, 1):
        print(f"[{i}/{len(other_files)}] Processing {file_path.name}...", end=' ')
        count, replacements = replace_in_file(file_path, translation_map)
        if count > 0:
            print(f"✓ {count} replacements")
            results['others'][str(file_path.relative_to(Path('/Volumes/ORICO/project/kefu/java/security-enterprise-tenant/crmchat/src')))] = {
                'count': count,
                'replacements': replacements
            }
        else:
            print("(no changes)")

    # Calculate summary
    total_files_processed = len(service_files) + len(controller_files) + len(other_files)
    total_files_changed = len(results['services']) + len(results['controllers']) + len(results['others'])
    total_replacements = sum(f['count'] for f in results['services'].values()) + \
                        sum(f['count'] for f in results['controllers'].values()) + \
                        sum(f['count'] for f in results['others'].values())

    summary = {
        'timestamp': datetime.now().isoformat(),
        'total_files_processed': total_files_processed,
        'total_files_changed': total_files_changed,
        'total_replacements': total_replacements,
        'service_files': len(service_files),
        'service_files_changed': len(results['services']),
        'controller_files': len(controller_files),
        'controller_files_changed': len(results['controllers']),
        'other_files': len(other_files),
        'other_files_changed': len(results['others']),
        'results': results
    }

    # Save results
    output_file = Path('/Volumes/ORICO/project/kefu/claudedocs/translation_execution_results.json')
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(summary, f, ensure_ascii=False, indent=2)

    print("\n" + "=" * 80)
    print("EXECUTION SUMMARY")
    print("=" * 80)
    print(f"Total files processed: {total_files_processed}")
    print(f"Total files changed: {total_files_changed}")
    print(f"Total replacements: {total_replacements}")
    print(f"\nService files: {len(service_files)} processed, {len(results['services'])} changed")
    print(f"Controller files: {len(controller_files)} processed, {len(results['controllers'])} changed")
    print(f"Other files: {len(other_files)} processed, {len(results['others'])} changed")
    print(f"\nResults saved to: {output_file}")

    return summary

if __name__ == '__main__':
    process_all_files()
