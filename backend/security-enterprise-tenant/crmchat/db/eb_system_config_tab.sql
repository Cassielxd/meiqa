/*
 Navicat Premium Dump SQL

 Source Server         : phpim
 Source Server Type    : MySQL
 Source Server Version : 50740 (5.7.40-log)
 Source Host           : 47.97.102.216:3306
 Source Schema         : crmeb

 Target Server Type    : MySQL
 Target Server Version : 50740 (5.7.40-log)
 File Encoding         : 65001

 Date: 11/10/2025 18:00:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for eb_system_config_tab
-- ----------------------------
DROP TABLE IF EXISTS `eb_system_config_tab`;
CREATE TABLE `eb_system_config_tab`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(10) NOT NULL DEFAULT 0 COMMENT '上级分类id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置分类名称',
  `eng_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配置分类英文名称',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '配置分类状态',
  `info` tinyint(1) NOT NULL DEFAULT 0 COMMENT '配置分类是否显示',
  `icon` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '图标',
  `type` int(2) NOT NULL DEFAULT 0 COMMENT '配置类型',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '配置分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_system_config_tab
-- ----------------------------
INSERT INTO `eb_system_config_tab` VALUES (1, 0, '基础配置', 'basics', 1, 0, 'ios-settings', 0, 100);
INSERT INTO `eb_system_config_tab` VALUES (17, 0, '文件上传配置', 'upload_set', 1, 0, 'md-cloud-upload', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (31, 17, '基础配置', 'base_config', 1, 0, '', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (32, 17, '阿里云配置', 'aliyun_uploads', 1, 0, '', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (33, 17, '七牛云配置', 'qiniu_uplaods', 1, 0, '', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (34, 17, '腾讯云配置', 'tengxun_uploads', 1, 0, '', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (69, 22, '客服端配置', 'kefu_config', 1, 0, '', 0, 0);
INSERT INTO `eb_system_config_tab` VALUES (70, 0, 'uniPush配置', 'uni_push_config', 1, 0, '', 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
