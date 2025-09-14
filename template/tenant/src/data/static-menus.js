// 静态菜单数据 - 所有菜单都可访问，无权限限制
export const staticMenusData = [
  {
    "id": 7,
    "pid": 0,
    "title": "统计",
    "path": "/tenant/home/",
    "menu_name": "统计",
    "menu_path": "/tenant/home/",
    "unique_auth": "tenant-index-index",
    "sort": 127,
    "type": 0,
    "html": "|-----"
  },
  {
    "id": 9,
    "pid": 0,
    "title": "用户管理",
    "path": "/tenant/user",
    "menu_name": "用户管理",
    "menu_path": "/tenant/user",
    "unique_auth": "tenant-user",
    "sort": 100,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 10,
    "pid": 9,
    "title": "用户列表",
    "path": "/tenant/user/list",
    "menu_name": "用户列表",
    "menu_path": "/tenant/user/list",
    "unique_auth": "tenant-user-user-index",
    "sort": 10,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 227,
    "pid": 9,
    "title": "用户分组",
    "path": "/tenant/user/group",
    "menu_name": "用户分组",
    "menu_path": "/tenant/user/group",
    "unique_auth": "user-user-group",
    "sort": 9,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1008,
    "pid": 9,
    "title": "用户标签",
    "path": "/tenant/user/label",
    "menu_name": "用户标签",
    "menu_path": "/tenant/user/label",
    "unique_auth": "user-user-label",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 165,
    "pid": 0,
    "title": "客服管理",
    "path": "/tenant/kefu",
    "menu_name": "客服管理",
    "menu_path": "/tenant/kefu",
    "unique_auth": "setting-store-service",
    "sort": 2,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 1104,
    "pid": 165,
    "title": "站点统计",
    "path": "/tenant/kefu/statistics",
    "menu_name": "站点统计",
    "menu_path": "/tenant/kefu/statistics",
    "unique_auth": "tenant-kefu-statistics",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1105,
    "pid": 165,
    "title": "客服二维码",
    "path": "/tenant/kefu/qrcode",
    "menu_name": "客服二维码",
    "menu_path": "/tenant/kefu/qrcode",
    "unique_auth": "tenant-kefu-qrcode",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1106,
    "pid": 165,
    "title": "聊天记录",
    "path": "/tenant/kefu/record",
    "menu_name": "聊天记录",
    "menu_path": "/tenant/kefu/record",
    "unique_auth": "tenant-kefu-record",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 678,
    "pid": 165,
    "title": "客服列表",
    "path": "/tenant/setting/store_service/index",
    "menu_name": "客服列表",
    "menu_path": "/tenant/setting/store_service/index",
    "unique_auth": "tenant-setting-store_service-index",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 679,
    "pid": 165,
    "title": "客服话术",
    "path": "/tenant/setting/store_service/speechcraft",
    "menu_name": "客服话术",
    "menu_path": "/tenant/setting/store_service/speechcraft",
    "unique_auth": "tenant-setting-store_service-speechcraft",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 738,
    "pid": 165,
    "title": "用户留言",
    "path": "/tenant/setting/store_service/feedback",
    "menu_name": "用户留言",
    "menu_path": "/tenant/setting/store_service/feedback",
    "unique_auth": "tenant-setting-store_service-feedback",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 12,
    "pid": 0,
    "title": "设置管理",
    "path": "/tenant/setting",
    "menu_name": "设置管理",
    "menu_path": "/tenant/setting",
    "unique_auth": "tenant-setting",
    "sort": 0,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 1011,
    "pid": 12,
    "title": "代码获取",
    "path": "/tenant/system/code",
    "menu_name": "代码获取",
    "menu_path": "/tenant/system/code",
    "unique_auth": "tenant-system-code",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
];

// 根据菜单名称获取对应的图标
function getMenuIcon(menuName) {
  const iconMap = {
    '统计': 'ios-stats',
    '用户管理': 'ios-people',
    '用户列表': 'ios-list',
    '用户分组': 'ios-folder',
    '用户标签': 'ios-pricetag',
    '客服管理': 'ios-headset',
    '站点统计': 'ios-analytics',
    '客服二维码': 'ios-qr-scanner',
    '聊天记录': 'ios-chatbubbles',
    '客服列表': 'ios-contacts',
    '客服话术': 'ios-chatboxes',
    '用户留言': 'ios-mail',
    '设置管理': 'ios-settings',
    '系统设置': 'ios-cog',
    '页面管理': 'ios-document',
    '客服图标': 'ios-image',
    '隐私协议': 'ios-lock',
    '客服页面广告': 'ios-bulb',
    '管理权限': 'ios-key',
    '角色管理': 'ios-person',
    '管理员列表': 'ios-people',
    '权限规则': 'ios-list-box',
    'APP在线升级': 'ios-cloud-upload',
    '代码获取': 'ios-code',
    '维护管理': 'ios-construct',
    '开发配置': 'ios-hammer',
    '配置分类': 'ios-folder',
    '组合数据': 'ios-grid',
    '安全维护': 'ios-shield',
    '系统日志': 'ios-document'
  };
  return iconMap[menuName] || 'ios-folder-outline';
}

// 转换菜单数据结构，添加组件需要的字段
function transformMenuData(menuData) {
  return menuData.map(item => {
    const transformed = {
      ...item,
      title: item.menu_name || item.title,
      path: item.menu_path || item.path,
      icon: getMenuIcon(item.menu_name || item.title),
      children: item.children ? transformMenuData(item.children) : undefined
    };
    return transformed;
  });
}

// 构建层级菜单结构 - 根据pid字段构建真正的父子关系
function buildMenuTree(flatMenus) {
  // 创建菜单项映射，添加默认图标
  const menuMap = {};
  flatMenus.forEach(menu => {
    menuMap[menu.id] = {
      ...menu,
      icon: menu.icon || getMenuIcon(menu.title || menu.menu_name),
      children: []
    };
  });
  
  const result = [];
  
  // 构建父子关系
  flatMenus.forEach(menu => {
    const menuItem = menuMap[menu.id];
    
    if (menu.pid === 0) {
      // 根菜单
      result.push(menuItem);
    } else {
      // 子菜单，添加到父菜单的children中
      const parent = menuMap[menu.pid];
      if (parent) {
        parent.children.push(menuItem);
      }
    }
  });
  
  // 对每个层级的菜单进行排序
  function sortMenus(menus) {
    menus.sort((a, b) => (b.sort || 0) - (a.sort || 0));
    menus.forEach(menu => {
      if (menu.children && menu.children.length > 0) {
        sortMenus(menu.children);
      }
    });
  }
  
  sortMenus(result);
  
  // 调试：输出构建结果
  console.log('构建的菜单树形结构:', result);
  result.forEach((menu, index) => {
    console.log(`根菜单${index + 1}: ${menu.title}, 子菜单数量: ${menu.children.length}`);
    if (menu.children.length > 0) {
      menu.children.forEach((child, childIndex) => {
        console.log(`  - 子菜单${childIndex + 1}: ${child.title}`);
      });
    }
  });
  
  return result;
}

// 转换后的菜单数据
const transformedMenus = transformMenuData(buildMenuTree(staticMenusData));

// 调试：输出菜单结构
console.log('构建的菜单树形结构:', transformedMenus);
console.log('根菜单数量:', transformedMenus.length);

// 导出树形菜单数据
export { transformedMenus }

// 模拟API返回的数据结构
export function getStaticMenusData() {
  return Promise.resolve({
    data: transformedMenus,
    status: 200,
    msg: "获取菜单数据成功"
  });
}