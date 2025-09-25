// 静态菜单数据 - 所有菜单都可访问，无权限限制

// 获取翻译后的菜单数据
export function getStaticMenusData() {
  return [
    {
      "id": 7,
      "pid": 0,
      "title": "menu.statistics",
      "path": "/tenant/home/",
      "menu_name": "menu.statistics",
    "menu_path": "/tenant/home/",
    "unique_auth": "tenant-index-index",
    "sort": 127,
    "type": 0,
    "html": "|-----"
  },
  {
    "id": 9,
    "pid": 0,
    "title": "menu.userManagement",
    "path": "/tenant/user",
    "menu_name": "menu.userManagement",
    "menu_path": "/tenant/user",
    "unique_auth": "tenant-user",
    "sort": 100,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 10,
    "pid": 9,
    "title": "menu.userList",
    "path": "/tenant/user/list",
    "menu_name": "menu.userList",
    "menu_path": "/tenant/user/list",
    "unique_auth": "tenant-user-user-index",
    "sort": 10,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 227,
    "pid": 9,
    "title": "menu.userGroup",
    "path": "/tenant/user/group",
    "menu_name": "menu.userGroup",
    "menu_path": "/tenant/user/group",
    "unique_auth": "user-user-group",
    "sort": 9,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1008,
    "pid": 9,
    "title": "menu.userLabel",
    "path": "/tenant/user/label",
    "menu_name": "menu.userLabel",
    "menu_path": "/tenant/user/label",
    "unique_auth": "user-user-label",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 165,
    "pid": 0,
    "title": "menu.customerService",
    "path": "/tenant/kefu",
    "menu_name": "menu.customerService",
    "menu_path": "/tenant/kefu",
    "unique_auth": "setting-store-service",
    "sort": 2,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 1104,
    "pid": 165,
    "title": "menu.statistics",
    "path": "/tenant/kefu/statistics",
    "menu_name": "menu.statistics",
    "menu_path": "/tenant/kefu/statistics",
    "unique_auth": "tenant-kefu-statistics",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1105,
    "pid": 165,
    "title": "menu.qrcode",
    "path": "/tenant/kefu/qrcode",
    "menu_name": "menu.qrcode",
    "menu_path": "/tenant/kefu/qrcode",
    "unique_auth": "tenant-kefu-qrcode",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 1106,
    "pid": 165,
    "title": "menu.record",
    "path": "/tenant/kefu/record",
    "menu_name": "menu.record",
    "menu_path": "/tenant/kefu/record",
    "unique_auth": "tenant-kefu-record",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 678,
    "pid": 165,
    "title": "menu.serviceList",
    "path": "/tenant/setting/store_service/index",
    "menu_name": "menu.serviceList",
    "menu_path": "/tenant/setting/store_service/index",
    "unique_auth": "tenant-setting-store_service-index",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 679,
    "pid": 165,
    "title": "menu.serviceSpeechcraft",
    "path": "/tenant/setting/store_service/speechcraft",
    "menu_name": "menu.serviceSpeechcraft",
    "menu_path": "/tenant/setting/store_service/speechcraft",
    "unique_auth": "tenant-setting-store_service-speechcraft",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 738,
    "pid": 165,
    "title": "menu.userFeedback",
    "path": "/tenant/setting/store_service/feedback",
    "menu_name": "menu.userFeedback",
    "menu_path": "/tenant/setting/store_service/feedback",
    "unique_auth": "tenant-setting-store_service-feedback",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  {
    "id": 12,
    "pid": 0,
    "title": "menu.settingsManagement",
    "path": "/tenant/setting",
    "menu_name": "menu.settingsManagement",
    "menu_path": "/tenant/setting",
    "unique_auth": "tenant-setting",
    "sort": 0,
    "type": 1,
    "html": "|-----"
  },
  {
    "id": 1011,
    "pid": 12,
    "title": "menu.systemCode",
    "path": "/tenant/system/code",
    "menu_name": "menu.systemCode",
    "menu_path": "/tenant/system/code",
    "unique_auth": "tenant-system-code",
    "sort": 0,
    "type": 0,
    "html": "|-----|-----"
  },
  ];
}

// 根据菜单名称获取对应的图标（支持中英文菜单名）
function getMenuIcon(menuName) {
  const iconMap = {
    // 中文菜单名映射
    '统计': 'ios-stats',
    '用户管理': 'ios-people',
    '用户列表': 'ios-list',
    '用户分组': 'ios-folder',
    '用户标签': 'ios-pricetag',
    '客服管理': 'ios-headset',
    '客服二维码': 'ios-qr-scanner',
    '聊天记录': 'ios-chatbubbles',
    '客服列表': 'ios-contacts',
    '客服话术': 'ios-chatboxes',
    '用户留言': 'ios-mail',
    '设置管理': 'ios-settings',
    '系统设置': 'ios-cog',
    '代码获取': 'ios-code',
    // 英文菜单名映射
    'Statistics': 'ios-stats',
    'User Management': 'ios-people',
    'User List': 'ios-list',
    'User Group': 'ios-folder',
    'User Label': 'ios-pricetag',
    'Customer Service': 'ios-headset',
    'QR Code': 'ios-qr-scanner',
    'Chat Record': 'ios-chatbubbles',
    'Service List': 'ios-contacts',
    'Service Speechcraft': 'ios-chatboxes',
    'User Feedback': 'ios-mail',
    'Settings Management': 'ios-settings',
    'System Settings': 'ios-cog',
    'System Code': 'ios-code'
  };
  return iconMap[menuName] || 'ios-document';
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

// 获取转换后的菜单数据
export function getTransformedMenus() {
  const menuData = getStaticMenusData();
  const transformedMenus = transformMenuData(buildMenuTree(menuData));

  // 调试：输出菜单结构
  console.log('构建的菜单树形结构:', transformedMenus);
  console.log('根菜单数量:', transformedMenus.length);

  return transformedMenus;
}

// 模拟API返回的数据结构
export function getStaticMenusAPI() {
  return Promise.resolve({
    data: getTransformedMenus(),
    status: 200,
    msg: "获取菜单数据成功"
  });
}
