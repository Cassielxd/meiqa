/**
 * i18n Helper Functions
 * 国际化辅助函数
 */

/**
 * 切换语言
 * @param {string} lang - 语言代码 (zh-CN, en-US, zh-TW)
 * @param {VueRouter} router - Vue Router 实例
 * @param {object} currentQuery - 当前路由查询参数
 */
export const switchLanguage = (lang, router, currentQuery = {}) => {
  // 验证语言参数
  const supportedLangs = ['zh-CN', 'en-US', 'zh-TW'];
  if (!supportedLangs.includes(lang)) {
    console.warn(`Unsupported language: ${lang}`);
    return;
  }

  // 保存到本地存储
  localStorage.setItem('local', lang);

  // 更新URL参数
  const newQuery = { ...currentQuery, lang };

  // 更新当前路由
  router.replace({
    path: router.currentRoute.path,
    query: newQuery
  });

  // 刷新页面以应用新语言
  setTimeout(() => {
    window.location.reload();
  }, 100);
};

/**
 * 从URL获取语言参数
 * @param {VueRoute} route - Vue Route 对象
 * @returns {string|null} 语言代码或null
 */
export const getLanguageFromRoute = (route) => {
  const lang = route.query.lang || route.query.language;
  const supportedLangs = ['zh-CN', 'en-US', 'zh-TW'];

  if (lang && supportedLangs.includes(lang)) {
    return lang;
  }

  return null;
};

/**
 * 生成带语言参数的URL
 * @param {string} path - 路径
 * @param {object} query - 查询参数
 * @param {string} lang - 语言代码
 * @returns {object} 包含语言参数的路由对象
 */
export const createRouteWithLang = (path, query = {}, lang = null) => {
  const routeQuery = { ...query };

  if (lang) {
    routeQuery.lang = lang;
  } else {
    // 从当前语言获取
    const currentLang = localStorage.getItem('local') || 'zh-CN';
    routeQuery.lang = currentLang;
  }

  return { path, query: routeQuery };
};

/**
 * 语言映射表
 */
export const languageMap = {
  'zh-CN': '简体中文',
  'en-US': 'English',
  'zh-TW': '繁體中文'
};

/**
 * 获取当前语言显示名称
 * @param {string} lang - 语言代码
 * @returns {string} 语言显示名称
 */
export const getLanguageDisplayName = (lang) => {
  return languageMap[lang] || lang;
};