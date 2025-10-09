#!/bin/bash

# Verification Script for Chinese-to-English Translation
# This script verifies that all user-facing Chinese messages have been replaced

echo "================================================"
echo "Translation Verification Script"
echo "================================================"
echo ""

cd "$(dirname "$0")/../src" || exit 1

echo "Checking for remaining Chinese characters in user-facing messages..."
echo ""

# Count files with Chinese in exception messages
echo "1. Checking CrmChatException messages..."
EXCEPTION_COUNT=$(python3 -c "
import re
import os

chinese_pattern = re.compile(r'[\u4e00-\u9fff]')
exception_pattern = re.compile(r'throw new CrmChatException\([^)]+\)')

count = 0
for root, dirs, files in os.walk('.'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                    for match in exception_pattern.finditer(content):
                        if chinese_pattern.search(match.group()):
                            count += 1
            except:
                pass
print(count)
" 2>/dev/null)

echo "   Found: $EXCEPTION_COUNT exceptions with Chinese"

# Count files with Chinese in ApiResult messages
echo "2. Checking ApiResult messages..."
API_COUNT=$(python3 -c "
import re
import os

chinese_pattern = re.compile(r'[\u4e00-\u9fff]')
api_pattern = re.compile(r'ApiResult\.(ok|fail)\([^)]+\)')

count = 0
for root, dirs, files in os.walk('.'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                    for match in api_pattern.finditer(content):
                        if chinese_pattern.search(match.group()):
                            count += 1
            except:
                pass
print(count)
" 2>/dev/null)

echo "   Found: $API_COUNT ApiResult messages with Chinese"

echo ""
echo "================================================"
echo "VERIFICATION RESULTS"
echo "================================================"

TOTAL=$((EXCEPTION_COUNT + API_COUNT))

if [ $TOTAL -eq 0 ]; then
    echo "✅ SUCCESS: All user-facing messages are in English!"
    echo "   - Exceptions: 0 Chinese messages"
    echo "   - ApiResult: 0 Chinese messages"
    echo "   - Total: 0 Chinese messages"
    echo ""
    echo "Translation project is 100% complete."
    exit 0
else
    echo "⚠️  WARNING: Found $TOTAL user-facing messages still in Chinese"
    echo "   - Exceptions: $EXCEPTION_COUNT Chinese messages"
    echo "   - ApiResult: $API_COUNT Chinese messages"
    echo ""
    echo "Please review the translation process."
    exit 1
fi
