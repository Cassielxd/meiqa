#
# Structure for table "eb_app_version"
#

CREATE TABLE `eb_app_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '更新摘要',
  `verisons_num` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '版本号',
  `url` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '下载地址',
  `info` varchar(1000) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '更新详细内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='app在线升级';

#
# Data for table "eb_app_version"
#


#
# Structure for table "eb_application"
#

CREATE TABLE `eb_application` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '应用名称',
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT '应用ID',
  `app_secret` varchar(255) NOT NULL DEFAULT '' COMMENT '应用KEY',
  `icon` varchar(255) NOT NULL DEFAULT '' COMMENT '应用图标',
  `introduce` varchar(255) NOT NULL DEFAULT '' COMMENT '应用介绍',
  `timestamp` int(10) NOT NULL DEFAULT '0' COMMENT 'TOKEN生成时间戳',
  `rand` int(4) NOT NULL DEFAULT '0' COMMENT 'TOKEN携带随机数',
  `token` varchar(500) NOT NULL DEFAULT '' COMMENT 'TOKEN',
  `token_md5` varchar(32) NOT NULL DEFAULT '' COMMENT '短TOKEN',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='应用';

#
# Data for table "eb_application"
#

INSERT INTO `eb_application` VALUES (3,'客服','202116257358989495','da52ac13388dbbfe45f34315f580e31e','https://qiniu.crmeb.net/attach/2021/07/069e7202107011810578311.png','',1625735898,9495,'eyJpdiI6Im1oNThXdWZSY250QkhuTm4wdXJkeFE9PSIsInZhbHVlIjoiM2lDMEFNdERZYWlLZmJhRnBMVVE4NG1IbTIwRlBEU3MxajdVSEplUHNYWDlEbHdCdHJsUWFSY0pIRlpIMjN4NHhneXpGaXJ4ZzYxTDRSdVJWVWJVdWxWcndmaGNnRWd1L1l2NmJ3U0VQQ0V2Ry96ZmNLeDNKRWtjVVFLZkVSbzgzd21pWVlCcjAxaUhmNEpSUC9aUGkzMm1VR3I2ZCtUc2pLamcrNGpVL29RPSIsIm1hYyI6IjlmMWFhZDlhY2UxYjRjYzFhMTAwODE5MzJjNDM3MWMxNGJiZjJjZjhhZTI5ODc3OWMxMDZlODRiYjFkZTI3M2EifQ==','2f9eac61b216208cac9c1f0859070a8b',0,'2021-07-08 09:18:18','2021-07-08 09:18:18');

#
# Structure for table "eb_auxiliary"
#

CREATE TABLE `eb_auxiliary` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `binding_id` int(10) NOT NULL DEFAULT '0' COMMENT '绑定id',
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPid',
  `relation_id` int(10) NOT NULL DEFAULT '0' COMMENT '关联id',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '类型0=客服转接辅助，1=商品和分类辅助，2=优惠券和商品辅助',
  `other` varchar(2048) NOT NULL DEFAULT '' COMMENT '其他数据为json',
  `status` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '数据状态 0：未执行，1：成功， 2：失败， 3:删除',
  `update_time` int(10) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='辅助表';

#
# Data for table "eb_auxiliary"
#


#
# Structure for table "eb_cache"
#

CREATE TABLE `eb_cache` (
  `key` varchar(32) NOT NULL DEFAULT '' COMMENT '身份管理名称',
  `result` text NOT NULL COMMENT '缓存数据',
  `expire_time` int(11) NOT NULL DEFAULT '0' COMMENT '失效时间0=永久',
  `add_time` int(11) NOT NULL DEFAULT '0' COMMENT '缓存时间',
  KEY `key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据缓存表';

#
# Data for table "eb_cache"
#

INSERT INTO `eb_cache` VALUES ('kf_adv','\"<p><strong>\\u8fd9\\u5c31\\u662f\\u4e00\\u4e2a\\u6d4b\\u8bd5\\u754c\\u9762\\uff0c\\u60f3\\u4e86\\u89e3\\u66f4\\u591a\\u8bf7\\u5173\\u6ce8\\u6211\\u4eec<\\/strong><br\\/><\\/p>\"',0,1626662767);

#
# Structure for table "eb_category"
#

CREATE TABLE `eb_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(10) NOT NULL DEFAULT '0' COMMENT '上级id',
  `owner_id` int(10) NOT NULL DEFAULT '0' COMMENT '所属人，0为全部',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '分类名称',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '分类类型0=标签分类，1=快捷短语分类',
  `other` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '其他参数',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COMMENT='分类';

#
# Data for table "eb_category"
#


#
# Structure for table "eb_chat_auto_reply"
#

CREATE TABLE `eb_chat_auto_reply` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '关键词',
  `content` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '内容',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '所属用户',
  `appid` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '所属appid',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序,越靠前,越是能被自会回复到',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='自动回复';

#
# Data for table "eb_chat_auto_reply"
#


#
# Structure for table "eb_chat_complain"
#

CREATE TABLE `eb_chat_complain` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(100) NOT NULL DEFAULT '' COMMENT '投诉内容',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '用户表ID',
  `cate_id` int(10) NOT NULL DEFAULT '0' COMMENT '分类',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cate_id` (`cate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户投诉';

#
# Data for table "eb_chat_complain"
#


#
# Structure for table "eb_chat_service"
#

CREATE TABLE `eb_chat_service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPID',
  `mer_id` int(10) NOT NULL DEFAULT '0' COMMENT '商户id',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '客服uid',
  `group_id` int(10) NOT NULL DEFAULT '0' COMMENT '客服分组id',
  `online` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否在线',
  `account` varchar(50) NOT NULL DEFAULT '' COMMENT '账号',
  `password` varchar(255) NOT NULL DEFAULT '' COMMENT '密码',
  `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '客服头像',
  `nickname` varchar(50) NOT NULL DEFAULT '' COMMENT '代理名称',
  `phone` varchar(32) NOT NULL DEFAULT '' COMMENT '客服电话',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '客服状态，0隐藏1显示',
  `notify` tinyint(1) NOT NULL DEFAULT '0' COMMENT '订单通知1开启0关闭',
  `customer` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否展示统计管理',
  `uniqid` varchar(35) NOT NULL DEFAULT '' COMMENT '扫码登录唯一值',
  `is_app` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为APP登陆',
  `is_backstage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1=前台运行;0=后台运行',
  `auto_reply` tinyint(1) NOT NULL DEFAULT '0' COMMENT '自动回复',
  `welcome_words` varchar(255) NOT NULL DEFAULT '' COMMENT '欢迎语',
  `update_time` int(10) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `ip` varchar(50) NOT NULL DEFAULT '' COMMENT '访问IP',
  `client_id` varchar(50) NOT NULL DEFAULT '' COMMENT 'client_id',
  PRIMARY KEY (`id`),
  KEY `account` (`account`),
  KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='客服表';

#
# Data for table "eb_chat_service"
#

INSERT INTO `eb_chat_service` VALUES (10,'202116257358989495',0,1,0,1,'kefu','$2y$10$Iv0RLY8c/X06Qr3q740z7eftEWn1PixEixvKO.wtjklk6P1KwoKIK','https://chat.crmeb.net/uploads/attach/2021/09/20210906/c79d19dbfda66026ec891d188386cbb7.png','CRM 客服','15594500000',1626777835,1,0,0,'',0,1,0,'',0,'','');

#
# Structure for table "eb_chat_service_dialogue_record"
#

CREATE TABLE `eb_chat_service_dialogue_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPID',
  `mer_id` int(32) NOT NULL DEFAULT '0' COMMENT '商户id',
  `msn` text NOT NULL COMMENT '消息内容',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '发送人uid',
  `to_user_id` int(10) NOT NULL DEFAULT '0' COMMENT '接收人uid',
  `is_tourist` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1=游客模式，0=非游客',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '发送时间',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读（0：否；1：是；）',
  `remind` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否提醒过',
  `msn_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '消息类型 1=文字 2=表情 3=图片 4=语音',
  `is_send` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否发送',
  `other` varchar(2000) NOT NULL DEFAULT '' COMMENT '其他参数',
  `guid` varchar(100) NOT NULL DEFAULT '' COMMENT 'guid相当于唯一值',
  PRIMARY KEY (`id`),
  KEY `to_uid` (`to_user_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=466 DEFAULT CHARSET=utf8mb4 COMMENT='用户和客服对话记录';

#
# Data for table "eb_chat_service_dialogue_record"
#


#
# Structure for table "eb_chat_service_feedback"
#

CREATE TABLE `eb_chat_service_feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '用户uid',
  `rela_name` varchar(50) NOT NULL DEFAULT '0' COMMENT '姓名',
  `phone` varchar(11) NOT NULL DEFAULT '0' COMMENT '电话',
  `content` varchar(500) NOT NULL DEFAULT '0' COMMENT '反馈内容',
  `make` varchar(500) NOT NULL DEFAULT '0' COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态0=未查看，1=已查看',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='客服反馈表';

#
# Data for table "eb_chat_service_feedback"
#


#
# Structure for table "eb_chat_service_group"
#

CREATE TABLE `eb_chat_service_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '组名',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='客服分组';

#
# Data for table "eb_chat_service_group"
#


#
# Structure for table "eb_chat_service_record"
#

CREATE TABLE `eb_chat_service_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPID',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '发送人的uid',
  `to_user_id` int(10) NOT NULL DEFAULT '0' COMMENT '送达人的uid',
  `nickname` varchar(50) NOT NULL DEFAULT '' COMMENT '用户昵称',
  `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '用户头像',
  `is_tourist` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是游客',
  `online` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否在线',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 = pc,1=微信，2=小程序，3=H5',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  `update_time` int(10) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `delete_time` int(10) DEFAULT NULL COMMENT '删除字段',
  `mssage_num` int(10) NOT NULL DEFAULT '0' COMMENT '消息条数',
  `message` text NOT NULL COMMENT '消息内容',
  `message_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '消息类型',
  PRIMARY KEY (`id`),
  KEY `to_uid` (`to_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录';

#
# Data for table "eb_chat_service_record"
#


#
# Structure for table "eb_chat_service_speechcraft"
#

CREATE TABLE `eb_chat_service_speechcraft` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `kefu_id` int(10) NOT NULL DEFAULT '0' COMMENT '0为全局话术',
  `cate_id` int(10) NOT NULL DEFAULT '0' COMMENT '0为不分类全局话术',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '话术标题',
  `message` varchar(255) NOT NULL DEFAULT '' COMMENT '话术内容',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加时间',
  PRIMARY KEY (`id`),
  KEY `kefu_id` (`kefu_id`),
  KEY `cate_id` (`cate_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COMMENT='客服话术';

#
# Data for table "eb_chat_service_speechcraft"
#


#
# Structure for table "eb_chat_user"
#

CREATE TABLE `eb_chat_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(10) NOT NULL DEFAULT '0' COMMENT '用户UID',
  `group_id` int(10) NOT NULL DEFAULT '0' COMMENT '分组',
  `nickname` varchar(50) NOT NULL DEFAULT '' COMMENT '用户昵称',
  `remark_nickname` varchar(100) NOT NULL DEFAULT '' COMMENT '备注昵称',
  `openid` varchar(50) NOT NULL DEFAULT '' COMMENT 'openid',
  `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '头像',
  `phone` varchar(11) NOT NULL DEFAULT '' COMMENT '手机号',
  `last_ip` varchar(16) NOT NULL DEFAULT '' COMMENT '访问ip',
  `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'appid',
  `remarks` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `is_kefu` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否客服',
  `is_tourist` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否游客',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '用户类型 0 = pc , 1 = 微信 ，2 = 小程序 ，3 = H5, 4 = APP',
  `sex` tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别',
  `online` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1=在线,0=离线',
  `version` varchar(50) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`id`,`appid`)
) ENGINE=InnoDB AUTO_INCREMENT=795 DEFAULT CHARSET=utf8mb4 COMMENT='客服用户';

#
# Data for table "eb_chat_user"
#

INSERT INTO `eb_chat_user` VALUES (1,1,0,'CRM 客服','','','https://chat.crmeb.net/uploads/attach/2021/09/20210906/c79d19dbfda66026ec891d188386cbb7.png','15594500000','','202116257358989495','',0,0,0,0,0,0,'0','2021-07-20 10:43:56','2021-07-20 10:43:56');

#
# Structure for table "eb_chat_user_group"
#

CREATE TABLE `eb_chat_user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(100) NOT NULL DEFAULT '' COMMENT '分组名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='用户分组';

#
# Data for table "eb_chat_user_group"
#


#
# Structure for table "eb_chat_user_label"
#

CREATE TABLE `eb_chat_user_label` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(100) NOT NULL DEFAULT '' COMMENT '标签名称',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '用户表自增ID',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `cate_id` int(10) NOT NULL DEFAULT '0' COMMENT '标签分类',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cate_id` (`cate_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

#
# Data for table "eb_chat_user_label"
#


#
# Structure for table "eb_chat_user_label_assist"
#

CREATE TABLE `eb_chat_user_label_assist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label_id` int(10) NOT NULL DEFAULT '0' COMMENT '标签ID',
  `user_id` int(10) NOT NULL DEFAULT '0' COMMENT '用户表自增ID',
  PRIMARY KEY (`id`),
  KEY `uid_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COMMENT='用户标签辅助表';

#
# Data for table "eb_chat_user_label_assist"
#


#
# Structure for table "eb_qrcode"
#

CREATE TABLE `eb_qrcode` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '二维码名称',
  `url` varchar(255) NOT NULL DEFAULT '' COMMENT '地址',
  `user_ids` varchar(255) NOT NULL DEFAULT '' COMMENT '用户ids',
  `appid` varchar(35) NOT NULL DEFAULT '' COMMENT 'APPID',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='二维码';

#
# Data for table "eb_qrcode"
#


#
# Structure for table "eb_site_statistics"
#

CREATE TABLE `eb_site_statistics` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `source` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '网站来源',
  `path` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '来源网址',
  `ip` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT 'ip地址',
  `region` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '地区',
  `province` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '省份',
  `browser` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '浏览器',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='站点统计';

#
# Data for table "eb_site_statistics"
#


#
# Structure for table "eb_system_admin"
#

CREATE TABLE `eb_system_admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account` varchar(32) NOT NULL DEFAULT '' COMMENT '后台管理员账号',
  `head_pic` varchar(255) NOT NULL DEFAULT '' COMMENT '后台管理员头像',
  `pwd` varchar(100) NOT NULL DEFAULT '' COMMENT '后台管理员密码',
  `real_name` varchar(16) NOT NULL DEFAULT '' COMMENT '后台管理员姓名',
  `roles` varchar(128) NOT NULL DEFAULT '' COMMENT '后台管理员权限(menus_id)',
  `last_ip` varchar(16) NOT NULL DEFAULT '' COMMENT '后台管理员最后一次登录ip',
  `last_time` int(10) NOT NULL DEFAULT '0' COMMENT '后台管理员最后一次登录时间',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '后台管理员添加时间',
  `login_count` int(10) NOT NULL DEFAULT '0' COMMENT '登录次数',
  `level` int(3) NOT NULL DEFAULT '0' COMMENT '后台管理员级别',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '后台管理员状态 1有效0无效',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 1有效0无效',
  PRIMARY KEY (`id`),
  KEY `account` (`account`,`status`),
  KEY `account_2` (`account`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

#
# Data for table "eb_system_admin"
#

INSERT INTO `eb_system_admin` VALUES (1,'admin','','$2y$10$/BM3hGVZN2wq2gPXYIJZB.9YGwaTO/xM2NVz/k71dfWkmJpQCOGuS','','','1.80.112.217',1626775956,0,74,0,1,0);

#
# Structure for table "eb_system_attachment"
#

CREATE TABLE `eb_system_attachment` (
  `att_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '附件名称',
  `att_dir` varchar(200) NOT NULL DEFAULT '' COMMENT '附件路径',
  `satt_dir` varchar(200) NOT NULL DEFAULT '' COMMENT '压缩图片路径',
  `att_size` varchar(30) NOT NULL DEFAULT '' COMMENT '附件大小',
  `att_type` varchar(30) NOT NULL DEFAULT '' COMMENT '附件类型',
  `pid` int(10) NOT NULL DEFAULT '0' COMMENT '分类ID',
  `time` int(11) NOT NULL DEFAULT '0' COMMENT '上传时间',
  `image_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '图片上传类型 1本地 2七牛云 3OSS 4COS ',
  `module_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '图片上传模块类型 1 后台上传 2 用户生成',
  `real_name` varchar(255) NOT NULL DEFAULT '' COMMENT '原始文件名',
  PRIMARY KEY (`att_id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COMMENT='附件管理表';

#
# Data for table "eb_system_attachment"
#

INSERT INTO `eb_system_attachment` VALUES (49,'客服头像1','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/fcc758713087632dc785fff3d37db928.png','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/fcc758713087632dc785fff3d37db928.png','','',0,0,1,1,'4'),(50,'客服头像二','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/d4398c5d36757c1b1ed1f21202bea1c0.png','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/d4398c5d36757c1b1ed1f21202bea1c0.png','','',0,0,1,1,'3'),(51,'客服头像三','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/1b244797f8b86b4cc0665d75d160aa30.png','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/1b244797f8b86b4cc0665d75d160aa30.png','','',0,0,1,1,'2'),(52,'客服头像四','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/1f05bd27a6af2da438dc2bb689995fc5.png','https://demo40.crmeb.net/uploads/attach/2020/11/20201110/1f05bd27a6af2da438dc2bb689995fc5.png','','',0,0,1,1,'1'),(102,'1a00ec6542246a5828ad89df1b216275.png','/uploads/attach/2021/09/20210906/1a00ec6542246a5828ad89df1b216275.png','/uploads/attach/2021/09/20210906/1a00ec6542246a5828ad89df1b216275.png','0','image/jpeg',0,1630891764,1,1,'客服图标.png'),(103,'32645ce20cd8b945598d06bd2a31dd2a.png','/uploads/attach/2021/09/20210906/32645ce20cd8b945598d06bd2a31dd2a.png','/uploads/attach/2021/09/20210906/32645ce20cd8b945598d06bd2a31dd2a.png','0','image/jpeg',0,1630891772,1,1,'白底图标.png'),(104,'c79d19dbfda66026ec891d188386cbb7.png','/uploads/attach/2021/09/20210906/c79d19dbfda66026ec891d188386cbb7.png','/uploads/attach/2021/09/20210906/c79d19dbfda66026ec891d188386cbb7.png','0','image/jpeg',0,1630891871,1,1,'客服图标.png'),(105,'6972cb96c04079eb1952ef43a04c6fbf.png','/uploads/attach/2021/09/20210906/6972cb96c04079eb1952ef43a04c6fbf.png','/uploads/attach/2021/09/20210906/6972cb96c04079eb1952ef43a04c6fbf.png','0','image/jpeg',0,1630891891,1,1,'客服logo.png');

#
# Structure for table "eb_system_attachment_category"
#

CREATE TABLE `eb_system_attachment_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(10) NOT NULL DEFAULT '0' COMMENT '父级ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '分类名称',
  `enname` varchar(50) NOT NULL DEFAULT '' COMMENT '分类目录',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='附件分类表';

#
# Data for table "eb_system_attachment_category"
#


#
# Structure for table "eb_system_config"
#

CREATE TABLE `eb_system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(255) NOT NULL DEFAULT '' COMMENT '字段名称',
  `type` varchar(255) NOT NULL DEFAULT '' COMMENT '类型(文本框,单选按钮...)',
  `input_type` varchar(20) NOT NULL DEFAULT 'input' COMMENT '表单类型',
  `config_tab_id` int(10) NOT NULL DEFAULT '0' COMMENT '配置分类id',
  `parameter` varchar(255) NOT NULL DEFAULT '' COMMENT '规则 单选框和多选框',
  `upload_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '上传文件格式1单图2多图3文件',
  `required` varchar(255) NOT NULL DEFAULT '' COMMENT '规则',
  `width` int(10) NOT NULL DEFAULT '0' COMMENT '多行文本框的宽度',
  `high` int(10) NOT NULL DEFAULT '0' COMMENT '多行文框的高度',
  `value` text NOT NULL COMMENT '默认值',
  `info` varchar(255) NOT NULL DEFAULT '' COMMENT '配置名称',
  `desc` varchar(255) NOT NULL DEFAULT '' COMMENT '配置简介',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  PRIMARY KEY (`id`),
  KEY `key_status` (`menu_name`(191),`status`),
  KEY `menu_name` (`menu_name`(191))
) ENGINE=InnoDB AUTO_INCREMENT=375 DEFAULT CHARSET=utf8mb4 COMMENT='配置表';

#
# Data for table "eb_system_config"
#

INSERT INTO `eb_system_config` VALUES (1,'site_name','text','input',1,'',0,'required:true',100,0,'\"CRMEB\"','网站名称','网站名称很多地方会显示的，建议认真填写',10,1),(2,'site_url','text','input',1,'',0,'required:true,url:true',100,0,'\"\"','网站地址','安装自动配置，不要轻易修改，更换会影响网站访问、接口请求、本地文件储存、支付回调、微信授权、支付、小程序图片访问、部分二维码、官方授权等',5,1),(3,'site_logo','upload','',1,'',1,'',0,0,'\"https:\\/\\/chat.crmeb.net\\/uploads\\/attach\\/2021\\/09/\\20210906\\/6972cb96c04079eb1952ef43a04c6fbf.png\"','后台大LOGO','菜单展开左上角logo,建议尺寸[170*50]',3,1),(5,'seo_title','text','input',1,'',0,'required:true',100,0,'\"CRMEB\"','SEO标题','SEO标题',0,0),(108,'upload_type','radio','',31,'1=>本地存储\n2=>七牛云存储\n3=>阿里云OSS\n4=>腾讯COS',1,'',0,0,'1','上传类型','文件储存配置，注意：一旦配置就不要轻易修改，会导致文件不能使用',40,1),(109,'uploadUrl','text','input',32,'',0,'url:true',100,0,'\"\"','空间域名 Domain','空间域名 Domain',0,1),(110,'accessKey','text','input',32,'',0,'',100,0,'\"\"','AccessKey ID','AccessKey ID',0,1),(111,'secretKey','text','input',32,'',0,'',100,0,'\"\"','AccessKey Secret','AccessKey Secret',0,1),(112,'storage_name','text','input',32,'',0,'',100,0,'\"\"','Bucket','存储空间名称',0,1),(118,'storage_region','text','input',32,'',0,'',100,0,'\"\"','Endpoint','所属地域',0,1),(142,'tengxun_map_key','text','input',68,'',0,'',100,0,'','腾讯地图KEY','腾讯地图KEY，申请地址：https://lbs.qq.com',0,1),(144,'cache_config','text','input',1,'',0,'',100,0,'\"86400\"','网站缓存时间','配置全局缓存时间（秒），默认留空为永久缓存',0,1),(168,'site_logo_square','upload','',1,'',1,'',0,0,'\"https:\\/\\/chat.crmeb.net/\\uploads\\/attach\\/2021\\/09/\\/20210906\\/32645ce20cd8b945598d06bd2a31dd2a.png\"','后台小LOGO','后台菜单缩进小LOGO，尺寸180*180',1,1),(171,'login_logo','upload','',1,'',1,'',0,0,'','后台登录页LOGO','后台登录页LOGO，建议尺寸270x75',4,1),(172,'qiniu_uploadUrl','text','input',33,'',0,'',100,0,'\"\"','空间域名 Domain','空间域名 Domain',0,1),(173,'qiniu_accessKey','text','input',33,'',0,'',100,0,'\"\"','accessKey','accessKey',0,1),(174,'qiniu_secretKey','text','input',33,'',0,'',100,0,'\"\"','secretKey','secretKey',0,1),(175,'qiniu_storage_name','text','input',33,'',0,'',100,0,'\"\"','空间名称','存储空间名称',0,1),(176,'qiniu_storage_region','text','input',33,'',0,'',100,0,'\"\"','存储区域','所属地域',0,1),(177,'tengxun_uploadUrl','text','input',34,'',0,'',100,0,'\"\"','空间域名 Domain','空间域名 Domain',0,1),(178,'tengxun_accessKey','text','input',34,'',0,'',100,0,'\"\"','SecretId','SecretId',0,1),(179,'tengxun_secretKey','text','',34,'',0,'',100,0,'\"\"','SecretKey','SecretKey',0,1),(180,'tengxun_storage_name','text','input',34,'',0,'',100,0,'\"\"','存储桶名称','存储桶名称',0,1),(181,'tengxun_storage_region','text','input',34,'',0,'',100,0,'\"\"','所属地域','所属地域',0,1),(305,'service_feedback','textarea','',69,'',0,'',100,7,'\"\\u5c0a\\u656c\\u7684\\u7528\\u6237\\uff0c\\u5ba2\\u670d\\u5f53\\u524d\\u4e0d\\u5728\\u7ebf\\uff0c\\u6709\\u95ee\\u9898\\u8bf7\\u7559\\u8a00\\uff0c\\u6211\\u4eec\\u4f1a\\u7b2c\\u4e00\\u65f6\\u95f4\\u8fdb\\u884c\\u5904\\u7406\\uff01\\uff01\\uff01\"','客服反馈','客服反馈头部文字',0,1),(306,'tourist_avatar','upload','',69,'',2,'',0,0,'[\"https:\\/\\/demo40.crmeb.net\\/uploads\\/attach\\/2020\\/11\\/20201110\\/1b244797f8b86b4cc0665d75d160aa30.png\",\"https:\\/\\/demo40.crmeb.net\\/uploads\\/attach\\/2020\\/11\\/20201110\\/d4398c5d36757c1b1ed1f21202bea1c0.png\",\"https:\\/\\/demo40.crmeb.net\\/uploads\\/attach\\/2020\\/11\\/20201110\\/fcc758713087632dc785fff3d37db928.png\",\"https:\\/\\/chat.crmeb.net\\/uploads\\/attach\\/2021\\/08\\/20210811\\/6ba99e3765d19fb35c23792b4143bb49.png\"]','客服游客头像','客服游客头像',0,1),(307,'qq_map_key','text','input',1,'',0,'',100,0,'\"\"','腾讯地图开放平台KEY','腾讯地图开放平台KEY,申请地址:https://lbs.qq.com/dev/console/application/mine',0,1),(308,'kefu_icon_url3','upload','',69,'',1,'',0,0,'\"\"','客服图标地址自定义','客服图标地址自定义',0,2),(309,'kefu_icon_url1','upload','',69,'',1,'',0,0,'\"data:image/png;base64,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\"','客服图标地址长方形','客服图标地址长方形',0,0),(310,'kefu_icon_url2','upload','',69,'',1,'',0,0,'\"data:image/png;base64,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\"','客服图标地址','客服图标地址',0,0),(311,'kefu_icon_type','radio','',69,'0=>默认图标\n1=>悬浮球\n2=>自定义',0,'',0,0,'\"1\"','客服图标','客服图标',0,2),(312,'uni_push_app_secret','text','input',70,'',0,'',100,0,'','appSecret','uniPush应用appSecret',0,1),(313,'uni_push_masterSecret','text','input',70,'',0,'',100,0,'','masterSecret','uniPush应用masterSecret',0,1),(314,'uni_push_appkey','text','input',70,'',0,'',100,0,'','appKey','uniPush应用key',0,1),(315,'uni_push_appid','text','input',70,'',0,'',100,0,'','appId','unipush应用ID',0,1);

#
# Structure for table "eb_system_config_tab"
#

CREATE TABLE `eb_system_config_tab` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(10) NOT NULL DEFAULT '0' COMMENT '上级分类id',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '配置分类名称',
  `eng_title` varchar(255) NOT NULL DEFAULT '' COMMENT '配置分类英文名称',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '配置分类状态',
  `info` tinyint(1) NOT NULL DEFAULT '0' COMMENT '配置分类是否显示',
  `icon` varchar(30) NOT NULL DEFAULT '' COMMENT '图标',
  `type` int(2) NOT NULL DEFAULT '0' COMMENT '配置类型',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COMMENT='配置分类表';

#
# Data for table "eb_system_config_tab"
#

INSERT INTO `eb_system_config_tab` VALUES (1,0,'基础配置','basics',1,0,'ios-settings',0,100),(17,0,'文件上传配置','upload_set',1,0,'md-cloud-upload',0,0),(31,17,'基础配置','base_config',1,0,'',0,0),(32,17,'阿里云配置','aliyun_uploads',1,0,'',0,0),(33,17,'七牛云配置','qiniu_uplaods',1,0,'',0,0),(34,17,'腾讯云配置','tengxun_uploads',1,0,'',0,0),(69,22,'客服端配置','kefu_config',1,0,'',0,0),(70,0,'uniPush配置','uni_push_config',1,0,'',0,0);

#
# Structure for table "eb_system_group"
#

CREATE TABLE `eb_system_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cate_id` int(10) NOT NULL DEFAULT '0' COMMENT '分类id',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '数据组名称',
  `info` varchar(255) NOT NULL DEFAULT '' COMMENT '数据提示',
  `config_name` varchar(50) NOT NULL DEFAULT '' COMMENT '数据字段',
  `fields` text NOT NULL COMMENT '数据组字段以及类型（json数据）',
  PRIMARY KEY (`id`),
  KEY `cate_id` (`cate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置分类表';

#
# Data for table "eb_system_group"
#


#
# Structure for table "eb_system_group_data"
#

CREATE TABLE `eb_system_group_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gid` int(10) NOT NULL DEFAULT '0' COMMENT '对应的数据组id',
  `value` text NOT NULL COMMENT '数据组对应的数据值（json数据）',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '添加数据时间',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '数据排序',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（1：开启；2：关闭；）',
  PRIMARY KEY (`id`),
  KEY `gid` (`gid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合数据详情表';

#
# Data for table "eb_system_group_data"
#


#
# Structure for table "eb_system_log"
#

CREATE TABLE `eb_system_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` int(10) NOT NULL DEFAULT '0' COMMENT '管理员id',
  `admin_name` varchar(64) NOT NULL DEFAULT '' COMMENT '管理员姓名',
  `path` varchar(128) NOT NULL DEFAULT '' COMMENT '链接',
  `page` varchar(64) NOT NULL DEFAULT '' COMMENT '行为',
  `method` varchar(12) NOT NULL DEFAULT '' COMMENT '访问类型',
  `ip` varchar(16) NOT NULL DEFAULT '' COMMENT '登录IP',
  `type` varchar(32) NOT NULL DEFAULT '' COMMENT '类型',
  `add_time` int(10) NOT NULL DEFAULT '0' COMMENT '操作时间',
  `merchant_id` int(10) NOT NULL DEFAULT '0' COMMENT '商户id',
  PRIMARY KEY (`id`),
  KEY `admin_id` (`admin_id`),
  KEY `add_time` (`add_time`),
  KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=7947 DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作记录表';

#
# Data for table "eb_system_log"
#


#
# Structure for table "eb_system_menus"
#

CREATE TABLE `eb_system_menus` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `pid` int(10) NOT NULL DEFAULT '0' COMMENT '父级id',
  `icon` varchar(16) NOT NULL DEFAULT '' COMMENT '图标',
  `menu_name` varchar(32) NOT NULL DEFAULT '' COMMENT '按钮名',
  `module` varchar(32) NOT NULL DEFAULT '' COMMENT '模块名',
  `controller` varchar(64) NOT NULL DEFAULT '' COMMENT '控制器',
  `action` varchar(32) NOT NULL DEFAULT '' COMMENT '方法名',
  `api_url` varchar(100) NOT NULL DEFAULT '' COMMENT 'api接口地址',
  `methods` varchar(255) NOT NULL DEFAULT '' COMMENT '提交方式POST GET PUT DELETE',
  `params` varchar(128) NOT NULL DEFAULT '' COMMENT '参数',
  `sort` int(10) NOT NULL DEFAULT '0' COMMENT '排序',
  `is_show` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为隐藏菜单0=隐藏菜单,1=显示菜单',
  `is_show_path` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为隐藏菜单供前台使用',
  `access` tinyint(1) NOT NULL DEFAULT '0' COMMENT '子管理员是否可用',
  `menu_path` varchar(255) NOT NULL DEFAULT '' COMMENT '路由名称 前端使用',
  `path` varchar(255) NOT NULL DEFAULT '' COMMENT '路径',
  `auth_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为菜单 1菜单 2功能',
  `header` varchar(10) NOT NULL DEFAULT '' COMMENT '顶部菜单标示',
  `is_header` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否顶部菜单1是0否',
  `unique_auth` varchar(255) NOT NULL DEFAULT '' COMMENT '前台唯一标识',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `pid` (`pid`),
  KEY `is_show` (`is_show`),
  KEY `access` (`access`)
) ENGINE=InnoDB AUTO_INCREMENT=1134 DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

#
# Data for table "eb_system_menus"
#

INSERT INTO `eb_system_menus` VALUES (7,0,'md-home','统计','admin','index','','','','[]',127,1,0,1,'/admin/home/','',1,'home',1,'admin-index-index',0),(9,0,'md-person','用户管理','admin','user.user','','','','[]',100,1,0,1,'/admin/user','',1,'user',1,'admin-user',0),(10,9,'','用户列表','admin','user.user','index','','','[]',10,1,0,1,'/admin/user/list','9',1,'user',1,'admin-user-user-index',0),(12,0,'md-settings','设置管理','admin','setting.system_config','index','','','[]',0,1,0,1,'/admin/setting','',1,'setting',1,'admin-setting',0),(14,12,'','管理权限','admin','setting.system_admin','','','','[]',0,1,0,1,'/admin/setting/auth/list','',1,'setting',1,'setting-system-admin',0),(19,14,'','角色管理','admin','setting.system_role','index','','','[]',1,1,0,1,'/admin/setting/system_role/index','',1,'setting',1,'setting-system-role',0),(20,14,'','管理员列表','admin','setting.system_admin','index','','','[]',1,1,0,1,'/admin/setting/system_admin/index','',1,'setting',0,'setting-system-list',0),(21,14,'','权限规则','admin','setting.system_menus','index','','','[]',1,1,0,1,'/admin/setting/system_menus/index','',1,'setting',0,'setting-system-menus',0),(23,12,'','系统设置','admin','setting.system_config','index','','','[]',10,1,0,1,'/admin/setting/system_config','',1,'setting',1,'setting-system-config',0),(25,0,'md-hammer','维护管理','admin','system','','','','[]',-1,1,0,1,'/admin/system','',1,'setting',1,'admin-system',0),(47,65,'','系统日志','admin','system.system_log','index','','','[]',0,1,0,1,'/admin/system/maintain/system_log/index','',1,'system',0,'system-maintain-system-log',0),(48,7,'','控制台','admin','index','index','','','[]',127,1,0,1,'/admin/home/index','',1,'home',0,'',1),(56,25,'','开发配置','admin','system','','','','[]',10,1,0,1,'/admin/system/config','',1,'system',1,'system-config-index',0),(65,25,'','安全维护','admin','system','','','','[]',7,1,0,1,'/admin/system/maintain','',1,'system',1,'system-maintain-index',0),(111,56,'','配置分类','admin','setting.system_config_tab','index','','','[]',0,1,0,1,'/admin/system/config/system_config_tab/index','',1,'system',0,'system-config-system_config-tab',0),(112,56,'','组合数据','admin','setting.system_group','index','','','[]',0,1,0,1,'/admin/system/config/system_group/index','',1,'system',0,'system-config-system_config-group',0),(125,56,'','配置列表','admin','system.config','index','','','[]',0,1,1,1,'/admin/system/config/system_config_tab/list','',1,'system',1,'system-config-system_config_tab-list',0),(126,56,'','组合数据列表','admin','system.system_group','list','','','[]',0,1,1,1,'/admin/system/config/system_group/list','',1,'system',1,'system-config-system_config-list',0),(165,0,'md-chatboxes','客服管理','admin','setting.storeService','index','','','[]',2,1,0,1,'/admin/kefu','',1,'',0,'setting-store-service',0),(227,9,'','用户分组','admin','user.user_group','index','','','[]',9,1,0,1,'/admin/user/group','9',1,'user',1,'user-user-group',0),(313,23,'','基本配置编辑头部数据','admin','','','api/admin/setting/config/header_basics','GET','[]',0,0,0,1,'','12/23',2,'',0,'',0),(314,23,'','基本配置编辑表单','admin','','','api/admin/setting/config/edit_basics','GET','[]',0,0,0,1,'','12/23',2,'',0,'',0),(315,23,'','基本配置保存数据','admin','','','api/admin/setting/config/save_basics','POST','[]',0,0,0,1,'','12/23',2,'',0,'',0),(325,19,'','添加身份','admin','','','','','[]',0,0,0,1,'/admin/setting/system_role/add','',1,'',0,'setting-system_role-add',0),(326,325,'','管理员身份权限列表','admin','','','api/admin/setting/role/create','GET','[]',0,0,0,1,'','12/14/19/325',2,'',0,'',0),(327,325,'','新建或编辑管理员','admin','','','api/admin/setting/role/<id>','POST','[]',0,0,0,1,'','12/14/19/325',2,'',0,'',0),(328,325,'','编辑管理员详情','admin','','','api/admin/setting/role/<id>/edit','GET','[]',0,0,0,1,'','12/14/19/325',2,'',0,'',0),(329,19,'','修改管理员身份状态','admin','','','api/admin/setting/role/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','12/14/19',2,'',0,'',0),(330,19,'','删除管理员身份','admin','','','api/admin/setting/role/<id>','DELETE','[]',0,0,0,1,'','12/14/19',2,'',0,'',0),(331,20,'','添加管理员','admin','','','','','[]',0,0,0,1,'/admin/setting/system_admin/add','',1,'',0,'setting-system_admin-add',0),(332,331,'','添加管理员表单','admin','','','api/admin/setting/admin/create','GET','[]',0,0,0,1,'','12/14/20/331',2,'',0,'',0),(333,331,'','添加管理员','admin','','','api/admin/setting/admin','POST','[]',0,0,0,1,'','12/14/20/331',2,'',0,'',0),(334,20,'','编辑管理员','admin','','','','','[]',0,0,0,1,'/admin /setting/system_admin/edit','',1,'',0,' setting-system_admin-edit',0),(335,334,'','编辑管理员表单','admin','','','api/admin/setting/admin/<id>/edit','GET','[]',0,0,0,1,'','12/14/20/334',2,'',0,'',0),(336,334,'','修改管理员','admin','','','api/admin/setting/admin/<id>','PUT','[]',0,0,0,1,'','12/14/20/334',2,'',0,'',0),(337,20,'','修改管理员接口','admin','','','api/admin/setting/admin/<id>','PUT','[]',0,0,0,1,'','12/14/20',2,'',0,'',0),(338,21,'','添加规则','admin','','','','','[]',0,0,0,1,'/admin/setting/system_menus/add','',1,'',0,'setting-system_menus-add',0),(339,338,'','添加权限表单','admin','','','api/admin/setting/menus/create','GET','[]',0,0,0,1,'','',2,'',0,'',0),(340,338,'','添加权限','admin','','','api/admin/setting/menus','POST','[]',0,0,0,1,'','12/14/21/338',2,'',0,'',0),(341,21,'','修改权限','admin','setting.system_menus','edit','','','[]',0,0,0,1,'/admin/setting/system_menus/edit','',1,'',0,'/setting-system_menus-edit',0),(342,341,'','编辑权限表单','admin','','','api/admin/setting/menus/<id>','GET','[]',0,0,0,1,'','12/14/21/341',2,'',0,'',0),(343,341,'','修改权限','admin','','','api/admin/setting/menus/<id>','PUT','[]',0,0,0,1,'','12/14/21/341',2,'',0,'',0),(344,21,'','修改权限状态','admin','','','api/admin/setting/menus/show/<id>','PUT','[]',0,0,0,1,'','12/14/21',2,'',0,'',0),(345,21,'','删除权限菜单','admin','','','api/admin/setting/menus/<id>','DELETE','[]',0,0,0,1,'','12/14/21',2,'',0,'',0),(346,338,'','添加子菜单','admin','setting.system_menus','add','','','[]',0,0,0,1,'/admin/setting/system_menus/add_sub','',1,'',0,'setting-system_menus-add_sub',0),(423,678,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(461,111,'','配置分类列表','admin','','','api/admin/setting/config_class','GET','[]',0,0,0,1,'','25/56/111',2,'',0,'',0),(462,111,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(463,462,'','配置分类添加表单','admin','','','api/admin/setting/config_class/create','GET','[]',0,0,0,1,'','25/56/111/462',2,'',0,'',0),(464,462,'','保存配置分类','admin','','','api/admin/setting/config_class','POST','[]',0,0,0,1,'','',2,'',0,'',0),(465,641,'','编辑配置分类','admin','','','api/admin/setting/config_class/<id>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(466,462,'','删除配置分类','admin','','','api/admin/setting/config_class/<id>','DELETE','[]',0,0,0,1,'','',2,'',0,'',0),(467,125,'','配置列表展示','admin','','','api/admin/setting/config','GET','[]',0,0,0,1,'','',2,'',0,'',0),(468,125,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(469,468,'','添加配置字段表单','admin','','','api/admin/setting/config/create','GET','[]',0,0,0,1,'','',2,'',0,'',0),(470,468,'','保存配置字段','admin','','','api/admin/setting/config','POST','[]',0,0,0,1,'','',2,'',0,'',0),(471,468,'','编辑配置字段表单','admin','','','api/admin/setting/config/<id>/edit','','[]',0,0,0,1,'','',2,'',0,'',0),(472,468,'','编辑配置分类','admin','','','api/admin/setting/config/<id>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(473,468,'','删除配置','admin','','','api/admin/setting/config/<id>','DELETE','[]',0,0,0,1,'','',2,'',0,'',0),(474,468,'','修改配置状态','admin','','','api/admin/setting/config/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(475,112,'','组合数据列表','admin','','','api/admin/setting/group','GET','[]',0,1,0,1,'','',2,'',0,'',0),(476,112,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(477,476,'','新增组合数据','admin','','','api/admin/setting/group','POST','[]',0,0,0,1,'','',2,'',0,'',0),(478,476,'','获取组合数据','admin','','','api/admin/setting/group/<id>','GET','[]',0,0,0,1,'','',2,'',0,'',0),(479,476,'','修改组合数据','admin','','','api/admin/setting/group/<id>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(480,476,'','删除组合数据','admin','','','api/admin/setting/group/<id>','DELETE','[]',0,0,0,1,'','',2,'',0,'',0),(481,126,'','组合数据列表表头','admin','','','api/admin/setting/group_data/header','GET','[]',0,0,0,1,'','',2,'',0,'',0),(482,126,'','组合数据列表','admin','','','api/admin/setting/group_data','GET','[]',0,0,0,1,'','',2,'',0,'',0),(483,126,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(484,483,'','获取组合数据添加表单','admin','','','api/admin/setting/group_data/create','GET','[]',0,0,0,1,'','',2,'',0,'',0),(485,483,'','保存组合数据','admin','','','api/admin/setting/group_data','POST','[]',0,0,0,1,'','',2,'',0,'',0),(486,483,'','获取组合数据信息','admin','','','api/admin/setting/group_data/<id>/edit','GET','[]',0,0,0,1,'','',2,'',0,'',0),(487,483,'','修改组合数据信息','admin','','','api/admin/setting/group_data/<id>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(488,483,'','删除组合数据','admin','','','api/admin/setting/group_data/<id>','DELETE','[]',0,0,0,1,'','',2,'',0,'',0),(489,483,'','修改组合数据状态','admin','','','api/admin/setting/group_data/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','',2,'',0,'',0),(492,47,'','系统日志管理员搜索条件','admin','','','api/admin/system/log/search_admin','GET','[]',0,0,0,1,'','25/65/47',2,'',0,'',0),(493,47,'','系统日志','admin','','','api/admin/system/log','GET','[]',0,0,0,1,'','25/65/47',2,'',0,'',0),(585,10,'','附加权限','admin','','','','','[]',0,0,0,1,'/admin*','',1,'',0,'',0),(610,20,'','管理员列表','admin','','','api/admin/setting/admin','GET','[]',0,0,0,1,'','12/14/20',2,'',0,'',0),(611,19,'','管理员身份列表','admin','','','api/admin/setting/role','GET','[]',0,0,0,1,'','12/14/19',2,'',0,'',0),(619,21,'','权限列表','admin','','','api/admin/setting/menus','GET','[]',0,0,0,1,'','12/14/21',2,'',0,'',0),(635,20,'','修改管理员状态','admin','','','api/admin/setting/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','12/14/20',2,'',0,'',0),(641,462,'','编辑配置分类','admin','','','','','[]',0,0,0,1,'system/config/system_config_tab/edit','',1,'',0,'',0),(642,641,'','获取修改配置分类接口','admin','','','api/admin/setting/config_class/<id>/edit','GET','[]',0,0,0,1,'','25/56/111/462/641',2,'',0,'',0),(656,12,'','页面管理','admin','','','','','[]',1,1,0,1,'/admin/setting/pages','',1,'',0,'admin-setting-pages',0),(678,165,'','客服列表','admin','','','','','[]',0,1,0,1,'/admin/setting/store_service/index','',1,'',0,'admin-setting-store_service-index',0),(679,165,'','客服话术','admin','','','','','[]',0,1,0,1,'/admin/setting/store_service/speechcraft','',1,'',0,'admin-setting-store_service-speechcraft',0),(738,165,'','用户留言','admin','','','','','[]',0,1,0,1,'/admin/setting/store_service/feedback','',1,'',0,'admin-setting-store_service-feedback',0),(739,738,'','获取用户反馈列表接口','admin','','','api/admin/chat/feedback','GET','[]',0,0,0,1,'','165/738',2,'',0,'',0),(740,738,'','附件权限','admin','','','','','[]',0,0,0,1,'*','',1,'',0,'',0),(741,740,'','删除用户反馈接口','admin','','','api/admin/chat/feedback/<id>','DELETE','[]',0,0,0,1,'','165/738/740',2,'',0,'',0),(742,679,'','获取话术列表接口','admin','','','api/admin/chat/speechcraft','GET','[]',0,0,0,1,'','165/679',2,'',0,'',0),(743,679,'','附件权限','admin','','','','','[]',0,0,0,1,'*','',1,'',0,'',0),(745,743,'','编辑话术','admin','','','','','[]',0,0,0,1,'/admin/setting/store_service/speechcraft/edit','',1,'',0,'admin-setting-store_service-speechcraft-edit',0),(748,745,'','获取话术创建接口','admin','','','api/admin/chat/speechcraft/create','GET','[]',0,0,0,1,'','165/679/743/745',2,'',0,'',0),(749,745,'','修改话术接口','admin','','','api/admin/chat/speechcraft/<id>','PUT','[]',0,0,0,1,'','165/679/743/745',2,'',0,'',0),(750,743,'','删除话术接口','admin','','','api/admin/chat/speechcraft/<id>','DELETE','[]',0,0,0,1,'','165/679/743',2,'',0,'',0),(778,740,'','修改用户反馈接口','admin','','','api/admin/chat/feedback/<id>','PUT','[]',0,0,0,1,'','165/738/740',2,'',0,'',0),(779,740,'','获取修改用户反馈接口','admin','','','api/admin/chat/feedback/<id>/edit','GET','[]',0,0,0,1,'','165/738/740',2,'',0,'',0),(789,743,'','话术分类','admin','','','','','[]',0,0,0,1,'/admin/setting/store_service/speechcraft/cate','',1,'',0,'admin-setting-store_service-speechcraft-cate',0),(790,789,'','获取话术分类列表接口','admin','','','api/admin/chat/speechcraftcate','GET','[]',0,0,0,1,'','165/679/743/789',2,'',0,'',0),(791,789,'','添加话术分类','admin','','','','','[]',0,0,0,1,'/admin/setting/store_service/speechcraft/cate/create','',1,'',0,'',0),(792,791,'','获取话术分类创建接口','admin','','','api/admin/chat/speechcraftcate/create','GET','[]',0,0,0,1,'','165/679/743/789/791',2,'',0,'',0),(793,791,'','保存话术分类接口','admin','','','api/admin/chat/speechcraftcate','POST','[]',0,0,0,1,'','165/679/743/789/791',2,'',0,'',0),(794,795,'','获取修改话术分类接口','admin','','','api/admin/chat/speechcraftcate/<id>/edit','GET','[]',0,0,0,1,'app/wechat/speechcraftcate/<id>/edit','165/679/743/789/795',2,'',0,'',0),(795,789,'','修改话术分类','admin','','','','','[]',0,0,0,1,'/admin/setting/store_service/speechcraft/cate/edit','',1,'',0,'',0),(796,795,'','修改话术分类接口','admin','','','api/admin/chat/speechcraftcate/<id>','PUT','[]',0,0,0,1,'','165/679/743/789/795',2,'',0,'',0),(797,789,'','删除话术分类接口','admin','','','api/admin/chat/speechcraftcate/<id>','DELETE','[]',0,0,0,1,'','165/679/743/789',2,'',0,'',0),(913,656,'','客服页面广告','admin','','','','','[]',0,1,0,1,'/admin/setting/system_group_data/kf_adv','',1,'',0,'setting-system-group_data-kf_adv',0),(915,913,'','设置客服广告','admin','','','api/admin/setting/set_kf_adv','POST','[]',0,0,0,1,'','12/656/913',2,'',0,'adminapi-setting-set_kf_adv',0),(916,913,'','获取客服广告','admin','','','api/admin/setting/get_kf_adv','GET','[]',0,0,0,1,'','12/656/913',2,'',0,'adminapi-setting-get_kf_adv',0),(1008,9,'','用户标签','admin','','','','','[]',0,1,0,1,'/admin/user/label','9',1,'',0,'user-user-label',0),(1009,1008,'','获取添加标签分类表单','admin','','','/api/admin/user/label/cate/create','GET','[]',0,0,0,1,'','9/1008',2,'',0,'admin-user-label_add',0),(1011,12,'','代码获取','admin','','','','','[]',0,1,0,1,'/admin/system/code','12',1,'',0,'admin-system-code',0),(1012,7,'','客户统计','admin','','','api/admin/chart/sum','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1013,7,'','客户首页统计','admin','','','api/admin/chart','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1014,585,'','获取修改用户表单','admin','','','api/admin/user/edit/<id>','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1015,585,'','修改用户','admin','','','api/admin/user/update/<id>','PUT','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1016,585,'','用户列表','admin','','','api/admin/user/index','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1018,585,'','批量修改用户分组','admin','','','api/admin/user/batch/group','PUT','[]',0,0,0,1,'','9/10/585',2,'',0,'admin-user-group_set',0),(1019,585,'','获取全部分组','admin','','','api/admin/user/group/all','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1020,227,'','获取分组列表接口','admin','','','api/admin/user/group','GET','[]',0,0,0,1,'','9/227',2,'',0,'admin-user-group',0),(1021,227,'','保存分组接口','admin','','','api/admin/user/group','POST','[]',0,0,0,1,'','9/227',2,'',0,'',0),(1022,227,'','获取分组创建接口','admin','','','api/admin/user/group/create','GET','[]',0,0,0,1,'','9/227',2,'',0,'',0),(1023,227,'','获取修分组签接口','admin','','','api/admin/user/group/<id>/edit','GET','[]',0,0,0,1,'','9/227',2,'',0,'',0),(1024,227,'','修改分组接口','admin','','','api/admin/user/group/<id>','PUT','[]',0,0,0,1,'','9/227',2,'',0,'',0),(1025,227,'','删除分组接口','admin','','','api/admin/user/group/<id>','DELETE','[]',0,0,0,1,'','9/227',2,'',0,'',0),(1026,1008,'','删除标签分类接口','admin','','','api/admin/user/label/cate/<id>','DELETE','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1027,1008,'','获取标签分类列表接口','admin','','','api/admin/user/label/cate','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1028,1008,'','获取修改标签分类接口','admin','','','api/admin/user/label/cate/<id>/edit','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1029,1008,'','保存标签分类接口','admin','','','api/admin/user/label/cate','POST','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1030,1008,'','获取标签创建接口','admin','','','api/admin/user/label/create','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1031,1008,'','获取标签分类创建接口','admin','','','api/admin/user/label/cate/create','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1032,1008,'','删除标签接口','admin','','','api/admin/user/label/<id>','DELETE','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1033,1008,'','获取修改标签接口','admin','','','api/admin/user/label/<id>/edit','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1034,1008,'','修改标签分类接口','admin','','','api/admin/user/label/cate/<id>','PUT','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1035,1008,'','修改标签接口','admin','','','api/admin/user/label/<id>','PUT','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1036,1008,'','保存标签接口','admin','','','api/admin/user/label','POST','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1037,1008,'','获取标签列表接口','admin','','','api/admin/user/label','GET','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1038,585,'','获取全部标签','admin','','','api/admin/user/label/all','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'admin-user-set_label',0),(1039,585,'','批量修改用户标签','admin','','','api/admin/user/batch/label','PUT','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1040,7,'','获取logo','admin','','','api/admin/logo','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1041,7,'','消息通知','admin','','','api/admin/jnotice','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1042,7,'','获取菜单','admin','','','api/admin/menusList','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1043,1011,'','获取应用列表接口','admin','','','api/admin/app','GET','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1044,1011,'','保存应用接口','admin','','','api/admin/app','POST','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1045,1011,'','获取应用创建接口','admin','','','api/admin/app/create','GET','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1046,1011,'','获取修改应用接口','admin','','','api/admin/app/<id>/edit','GET','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1047,1011,'','修改应用接口','admin','','','api/admin/app/<id>','PUT','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1048,1011,'','删除应用接口','admin','','','api/admin/app/<id>','DELETE','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1049,678,'','客服列表','admin','','','api/admin/chat/kefu','GET','[]',0,0,0,1,'','165/678',2,'',0,'admin-user-group',0),(1050,423,'','添加客服','admin','','','api/admin/chat/kefu','POST','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1051,423,'','客服登录','admin','','','api/admin/chat/kefu/login/<id>','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1052,423,'','添加客服表单','admin','','','api/admin/chat/kefu/add','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'setting-store_service-add',0),(1053,423,'','修改客服表单','admin','','','api/admin/chat/kefu/<id>/edit','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1054,423,'','修改客服','admin','','','api/admin/chat/kefu/<id>','PUT','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1055,423,'','删除客服','admin','','','api/admin/chat/kefu/<id>','DELETE','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1056,423,'','修改客服状态','admin','','','api/admin/chat/kefu/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1057,423,'','聊天记录','admin','','','api/admin/chat/kefu/record/<id>','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1058,423,'','查看对话','admin','','','api/admin/chat/kefu/chat_list','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1059,743,'','保存话术接口','admin','','','api/admin/chat/speechcraft','POST','[]',0,0,0,1,'','165/679/743',2,'',0,'setting-store_service-add',0),(1060,743,'','获取修改话术接口','admin','','','api/admin/chat/speechcraft/<id>/edit','GET','[]',0,0,0,1,'','165/679/743',2,'',0,'',0),(1061,743,'','获取话术详情接口','admin','','','api/admin/chat/speechcraft/<id>','GET','[]',0,0,0,1,'','165/679/743',2,'',0,'',0),(1062,789,'','获取话术分类详情接口','admin','','','api/admin/chat/speechcraftcate/<id>','GET','[]',0,0,0,1,'','165/679/743/789',2,'',0,'',0),(1063,25,'','附件管理','admin','','','','','[]',0,1,1,1,'/admin/system/attachment','25',1,'',0,'',0),(1064,1063,'','图片附件列表','admin','','','api/admin/file/file','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1065,1063,'','删除图片','admin','','','api/admin/file/file/delete','POST','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1066,1063,'','移动图片分类表单','admin','','','api/admin/file/file/move','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1067,1063,'','移动图片分类','admin','','','api/admin/file/file/do_move','PUT','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1068,1063,'','修改图片名称','admin','','','api/admin/file/file//<id>','PUT','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1069,1063,'','修改图片名称','admin','','','api/admin/file/file/update/<id>','PUT','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1070,1063,'','上传图片','admin','','','api/admin/file/upload/<upload_type?>','POST','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1071,1063,'','获取附件分类列表接口','admin','','','api/admin/file/category','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1072,1063,'','保存附件分类接口','admin','','','api/admin/file/category','POST','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1073,1063,'','获取附件分类创建接口','admin','','','api/admin/file/category/create','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1074,1063,'','获取修改附件分类接口','admin','','','api/admin/file/category/<id>/edit','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1075,1063,'','获取附件分类详情接口','admin','','','api/admin/file/category/<id>','GET','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1076,1063,'','修改附件分类接口','admin','','','api/admin/file/category/<id>','PUT','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1077,1063,'','删除附件分类接口','admin','','','api/admin/file/category/<id>','DELETE','[]',0,0,0,1,'','25/1063',2,'',0,'',0),(1078,20,'','删除管理员接口','admin','','','api/admin/setting/admin/<id>','DELETE','[]',0,0,0,1,'','12/14/20',2,'',0,'',0),(1079,21,'','获取修改权限菜单接口','admin','','','api/admin/setting/menus/<id>/edit','GET','[]',0,0,0,1,'','12/14/21',2,'',0,'',0),(1080,7,'','退出登陆','admin','','','api/admin/setting/admin/logout','GET','[]',0,1,0,1,'','7',2,'',0,'',0),(1082,25,'','管理员中心','admin','','','','','[]',0,1,1,1,'/admin/system/user','25',1,'',0,'',0),(1083,1082,'','修改当前管理员信息','admin','','','api/admin/setting/update_admin','PUT','[]',0,0,0,1,'','25/1082',2,'',0,'',0),(1084,1082,'','获取当前管理员信息','admin','','','api/admin/setting/info','GET','[]',0,0,0,1,'','25/1082',2,'',0,'',0),(1085,476,'','组合数据全部','admin','','','api/admin/setting/group_all','GET','[]',0,0,0,1,'','25/56/112/476',2,'',0,'',0),(1086,476,'','获取组合数据创建接口','admin','','','api/admin/setting/group/create','GET','[]',0,0,0,1,'','25/56/112/476',2,'',0,'',0),(1087,476,'','获取修改组合数据接口','admin','','','api/admin/setting/group/<id>/edit','GET','[]',0,0,0,1,'','25/56/112/476',2,'',0,'',0),(1088,21,'','未添加的权限规则列表','admin','','','api/admin/setting/ruleList','GET','[]',0,0,0,1,'','12/14/21',2,'',0,'',0),(1089,23,'','基本配置上传文件','admin','','','api/admin/setting/config/upload','POST','[]',0,0,0,1,'','12/23',2,'',0,'',0),(1090,23,'','获取修改系统配置接口','admin','','','api/admin/setting/config/<id>/edit','GET','[]',0,0,0,1,'','12/23',2,'',0,'',0),(1091,462,'','修改配置分类状态','admin','','','api/admin/setting/config_class/set_status/<id>/<status>','PUT','[]',0,0,0,1,'','25/56/111/462',2,'',0,'',0),(1092,462,'','获取配置分类详情接口','admin','','','api/admin/setting/config_class/<id>','GET','[]',0,0,0,1,'','25/56/111/462',2,'',0,'',0),(1093,476,'','获取组合数据资源详情接口','admin','','','api/admin/setting/group_data/<id>','GET','[]',0,0,0,1,'','25/56/112/476',2,'',0,'',0),(1097,423,'','自动回复列表','admin','','','api/admin/chat/reply','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1098,423,'','删除自动回复','admin','','','api/admin/chat/reply/<id>','DELETE','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1099,423,'','保存自动回复','admin','','','api/admin/chat/reply/<id>','POST','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1100,423,'','获取自动回复表单','admin','','','api/admin/chat/reply/<id>','GET','[]',0,0,0,1,'','165/678/423',2,'',0,'',0),(1101,585,'','用户标签搜索列表','admin','','','api/admin/user/user_label','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1102,1011,'','重置token','admin','','','api/admin/app/reset/<id>','PUT','[]',0,0,0,1,'','12/1011',2,'',0,'',0),(1103,12,'','APP在线升级','admin','','','','','[]',0,1,0,1,'/admin/setting/app/version','12',1,'',0,'setting-app-version',0),(1104,165,'','站点统计','admin','','','','','[]',0,1,0,1,'/admin/kefu/statistics','165',1,'',0,'admin-kefu-statistics',0),(1105,165,'','客服二维码','admin','','','','','[]',0,1,0,1,'/admin/kefu/qrcode','165',1,'',0,'admin-kefu-qrcode',0),(1106,165,'','聊天记录','admin','','','','','[]',0,1,0,1,'/admin/kefu/record','165',1,'',0,'admin-kefu-record',0),(1107,1096,'','设置客服图标','admin','','','/api/admin/setting/config/kefu','GET','[]',0,0,1,1,'','12/656/1096',2,'',0,'adminapi-setting-set_kf_icon',0),(1108,656,'','客服图标','admin','','','','','[]',0,1,0,1,'/admin/setting/system_group_data/kf_icon','12/656',1,'',0,'setting-system-group_data-kf_icon',0),(1110,47,'','系统日志管理员搜索条件','admin','','','api/admin/system/log/search_admin','GET','[]',0,0,0,1,'','25/65/47',2,'',0,'',0),(1111,585,'','用户标签搜索列表','admin','','','api/admin/user/user_label','GET','[]',0,0,0,1,'','9/10/585',2,'',0,'',0),(1112,47,'','系统日志','admin','','','api/admin/system/log','GET','[]',0,0,0,1,'','25/65/47',2,'',0,'',0),(1113,1103,'','保存APP升级','admin','','','api/admin/setting/verison/save/<id>','POST','[]',0,0,0,1,'','12/1103',2,'',0,'',0),(1114,1103,'','获取创建APP升级包表单','admin','','','api/admin/setting/verison/<id>','GET','[]',0,0,0,1,'','12/1103',2,'',0,'',0),(1115,1103,'','删除APP升级包','admin','','','api/admin/setting/verison/<id>','DELETE','[]',0,0,0,1,'','12/1103',2,'',0,'',0),(1116,1103,'','获取APP升级包列表','admin','','','api/admin/setting/verison','GET','[]',0,0,0,1,'','12/1103',2,'',0,'',0),(1117,1104,'','站点统计','admin','','','api/admin/chat/statistics','GET','[]',0,0,0,1,'','165/1102',2,'',0,'',0),(1118,678,'','删除自动回复','admin','','','api/admin/chat/reply/<id>','DELETE','[]',0,0,0,1,'','165/678',2,'',0,'',0),(1119,678,'','保存自动回复','admin','','','api/admin/chat/reply/<id>','POST','[]',0,0,0,1,'','165/678',2,'',0,'',0),(1120,678,'','获取自动回复表单','admin','','','api/admin/chat/reply/<id>','GET','[]',0,0,0,1,'','165/678',2,'',0,'',0),(1121,678,'','自动回复列表','admin','','','api/admin/chat/reply','GET','[]',0,0,0,1,'','165/678',2,'',0,'',0),(1122,1101,'','删除随机客服二维码','admin','','','api/admin/chat/qrcode/<id>','DELETE','[]',0,0,0,1,'','165/1101',2,'',0,'',0),(1123,1101,'','保存随机客服二维码','admin','','','api/admin/chat/qrcode/<id>','POST','[]',0,0,0,1,'','165/1101',2,'',0,'',0),(1124,1101,'','获取随机客服二维码表单','admin','','','api/admin/chat/qrcode/<id>','GET','[]',0,0,0,1,'','165/1101',2,'',0,'',0),(1125,1101,'','获取随机客服二维码','admin','','','api/admin/chat/qrcode','GET','[]',0,0,0,1,'','165/1101',2,'',0,'',0),(1126,1100,'','获取所有客服','admin','','','api/admin/chat/record_kefu','GET','[]',0,0,0,1,'','165/1100',2,'',0,'',0),(1127,1100,'','查看所有聊天记录','admin','','','api/admin/chat/record','GET','[]',0,0,0,1,'','165/1100',2,'',0,'',0),(1128,1096,'','保存客服图标上传配置','admin','','','api/admin/setting/config/kefu','POST','[]',0,0,0,1,'','12/656/1096',2,'',0,'',0),(1129,1008,'','标签分类移动排序','admin','','','api/admin/user/label/move_cate','POST','[]',0,0,0,1,'','9/1008',2,'',0,'',0),(1130,1008,'','标签移动排序','admin','','','api/admin/user/label/move','POST','[]',0,1,0,1,'','9/1008',2,'',0,'',0),(1131,656,'','隐私协议','admin','','','','','[]',0,1,0,1,'/admin/setting/system_group_data/privacy','12/656',1,'',0,'setting-system-group_data-privacy',0),(1132,1131,'','获取隐私协议','admin','','','api/admin/setting/get_user_agreement','GET','[]',0,0,0,1,'','12/656/1094',2,'',0,'',0),(1133,1131,'','设置隐私协议','admin','','','api/admin/setting/set_user_agreement','POST','[]',0,0,0,1,'','12/656/1094',2,'',0,'',0);

#
# Structure for table "eb_system_role"
#

CREATE TABLE `eb_system_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(32) NOT NULL DEFAULT '' COMMENT '身份管理名称',
  `rules` text NOT NULL COMMENT '身份管理权限(menus_id)',
  `level` int(3) NOT NULL DEFAULT '0' COMMENT '身份等级',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='身份管理表';

#
# Data for table "eb_system_role"
#

