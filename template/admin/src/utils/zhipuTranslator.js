/**
 * 智谱AI翻译工具类
 * @Date 2025-11-03
 */

const API_URL = 'https://open.bigmodel.cn/api/paas/v4/chat/completions';
const API_KEY = '33a1de3650004f7cbd97aca9b8d65403.Bb0V2RScWAe8Mv61';
const MODEL = 'glm-4-flash';

/**
 * 翻译文本到指定语言
 * 
 * @param {string} text 原文本
 * @param {string} targetLanguage 目标语言 (zh-CN: 中文, en-US: 英文)
 * @returns {Promise<string>} 翻译后的文本
 */
export async function translateText(text, targetLanguage = 'zh-CN') {
  if (!text || text.trim() === '' || text === '{}') {
    return '';  // 返回空字符串
  }
  
  // 如果目标语言是英文，直接返回原文
  if (targetLanguage === 'en-US') {
    return text;
  }
  
  try {
    const prompt = buildTranslatePrompt(text, targetLanguage);
    const response = await callZhipuApi(prompt);
    const translation = extractTranslation(response);
    
    // 检查翻译结果是否有效
    if (!translation || translation.trim() === '' ||
        translation.includes('很抱歉') || translation.includes('抱歉') ||
        translation.includes('请提供') || translation.includes('空白') ||
        translation === '{}') {
      console.warn('警告: 翻译结果无效，返回空字符串:', translation);
      return '';  // 返回空字符串
    }
    
    return translation;
  } catch (error) {
    console.error('翻译失败，原文:', text, ', 目标语言:', targetLanguage, ', 错误:', error.message);
    return '';  // 翻译失败时返回空字符串
  }
}

/**
 * 批量翻译文本
 * 
 * @param {Array<string>} texts 原文本列表
 * @param {string} targetLanguage 目标语言
 * @returns {Promise<Array<string>>} 翻译后的文本列表
 */
export async function translateTexts(texts, targetLanguage = 'zh-CN') {
  const results = [];
  
  for (const text of texts) {
    const translation = await translateText(text, targetLanguage);
    results.push(translation);
    
    // 添加短暂延迟，避免API调用过于频繁
    await sleep(100);
  }
  
  return results;
}

/**
 * 构建翻译提示词
 * 
 * @param {string} text 原文本
 * @param {string} targetLanguage 目标语言
 * @returns {string} 提示词
 */
function buildTranslatePrompt(text, targetLanguage) {
  const targetLangName = getLanguageName(targetLanguage);
  
  return `请将以下文本翻译成${targetLangName}，要求：
1. 翻译要准确、自然、符合目标语言的表达习惯
2. 如果是专业术语，请使用专业词汇
3. 只返回翻译结果，不要包含任何解释或额外信息
4. 保持原文的格式和标点符号

原文：${text}

翻译：`;
}

/**
 * 获取语言名称
 * 
 * @param {string} languageCode 语言代码
 * @returns {string} 语言名称
 */
function getLanguageName(languageCode) {
  const languageMap = {
    'zh-CN': '中文（简体）',
    'zh-TW': '中文（繁体）',
    'en-US': '英文',
    'ja-JP': '日文',
    'ko-KR': '韩文',
    'es-ES': '西班牙文',
    'fr-FR': '法文',
    'de-DE': '德文',
    'ru-RU': '俄文',
    'ar-SA': '阿拉伯文',
    'pt-PT': '葡萄牙文'
  };
  
  return languageMap[languageCode] || '中文（简体）';
}

/**
 * 调用智谱AI API
 * 
 * @param {string} prompt 提示词
 * @returns {Promise<Object>} API响应
 */
async function callZhipuApi(prompt) {
  const requestBody = {
    model: MODEL,
    temperature: 0.3,
    max_tokens: 1000,
    messages: [
      {
        role: 'user',
        content: prompt
      }
    ]
  };
  
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${API_KEY}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify(requestBody)
  });
  
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`API调用失败，状态码: ${response.status}, 响应: ${errorText}`);
  }
  
  return await response.json();
}

/**
 * 从API响应中提取翻译结果
 * 
 * @param {Object} response API响应对象
 * @returns {string|null} 翻译结果
 */
function extractTranslation(response) {
  try {
    if (response.choices && response.choices.length > 0) {
      const firstChoice = response.choices[0];
      let content = firstChoice.message && firstChoice.message.content;

      // 清理翻译结果，移除可能的前缀
      if (content) {
        content = content.trim();
        // 移除可能的"翻译："前缀
        if (content.startsWith('翻译：')) {
          content = content.substring(3).trim();
        }
      }

      return content;
    }
  } catch (error) {
    console.error('解析翻译响应失败:', error.message);
  }

  // 返回null而不是抛出异常
  console.warn('警告: 无法从API响应中提取翻译结果:', response);
  return null;
}

/**
 * 延迟函数
 * 
 * @param {number} ms 延迟毫秒数
 * @returns {Promise<void>}
 */
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 移除HTML标签，获取纯文本
 * 
 * @param {string} html HTML字符串
 * @returns {string} 纯文本
 */
export function stripHtmlTags(html) {
  if (!html) return '';
  
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = html;
  return tempDiv.textContent || tempDiv.innerText || '';
}

export default {
  translateText,
  translateTexts,
  stripHtmlTags
};

