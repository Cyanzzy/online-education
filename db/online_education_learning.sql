/*
Navicat MySQL Data Transfer

Source Server         : MySQL8.0
Source Server Version : 80026
Source Host           : localhost:3306
Source Database       : online_education_learning

Target Server Type    : MYSQL
Target Server Version : 80026
File Encoding         : 65001

Date: 2023-11-14 21:22:21
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ol_choose_course
-- ----------------------------
DROP TABLE IF EXISTS `ol_choose_course`;
CREATE TABLE `ol_choose_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `order_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `course_price` float(10,2) NOT NULL COMMENT '课程价格',
  `valid_days` int NOT NULL COMMENT '课程有效期(天)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课状态',
  `validtime_start` datetime NOT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '结束服务时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of ol_choose_course
-- ----------------------------
INSERT INTO `ol_choose_course` VALUES ('16', '121', 'Spring Cloud 开发实战', '52', '1232141425', '700002', '2023-02-09 11:43:32', '1.00', '365', '701001', '2023-02-09 11:43:32', '2024-02-09 11:43:32', null);
INSERT INTO `ol_choose_course` VALUES ('17', '121', 'Spring Cloud 开发实战', '52', '1232141425', '700002', '2023-02-09 11:49:06', '1.00', '365', '701002', '2023-02-09 11:49:06', '2024-02-09 11:49:06', null);
INSERT INTO `ol_choose_course` VALUES ('18', '2', '测试课程01', '52', '1232141425', '700001', '2023-07-08 14:45:32', '1.00', '365', '701001', '2023-07-08 14:45:32', '2024-07-07 14:45:32', null);

-- ----------------------------
-- Table structure for ol_course_tables
-- ----------------------------
DROP TABLE IF EXISTS `ol_course_tables`;
CREATE TABLE `ol_course_tables` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `choose_course_id` bigint NOT NULL COMMENT '选课记录id',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `course_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `course_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `validtime_start` datetime DEFAULT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '到期时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `course_tables_unique` (`user_id`,`course_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of ol_course_tables
-- ----------------------------
INSERT INTO `ol_course_tables` VALUES ('11', '16', '52', '121', '1232141425', 'Spring Cloud 开发实战', '700002', '2023-02-09 11:44:48', '2023-02-09 11:43:32', '2024-02-09 11:43:32', null, null);
INSERT INTO `ol_course_tables` VALUES ('12', '18', '52', '2', '1232141425', '测试课程01', '700001', '2023-07-08 14:45:32', '2023-07-08 14:45:32', '2024-07-07 14:45:32', '2023-07-08 14:46:21', null);

-- ----------------------------
-- Table structure for ol_learn_record
-- ----------------------------
DROP TABLE IF EXISTS `ol_learn_record`;
CREATE TABLE `ol_learn_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程名称',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户id',
  `learn_date` datetime DEFAULT NULL COMMENT '最近学习时间',
  `learn_length` bigint DEFAULT NULL COMMENT '学习时长',
  `teachplan_id` bigint DEFAULT NULL COMMENT '章节id',
  `teachplan_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '章节名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `learn_record_unique` (`course_id`,`user_id`,`teachplan_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of ol_learn_record
-- ----------------------------
INSERT INTO `ol_learn_record` VALUES ('1', '123', 'SpringBoot实战', '52', '2022-10-06 11:31:19', '22', '222', '入门程序');
INSERT INTO `ol_learn_record` VALUES ('2', '121', 'Java编程思想', '52', '2022-10-06 11:31:57', '10', '333', 'Java学习路径');
INSERT INTO `ol_learn_record` VALUES ('7', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:18:24', '0', '269', '1.1 什么是配置中心');
INSERT INTO `ol_learn_record` VALUES ('8', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:18:23', '0', '270', '1.2Nacos简介');
INSERT INTO `ol_learn_record` VALUES ('9', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:18:25', '0', '271', '1.3安装Nacos Server');
INSERT INTO `ol_learn_record` VALUES ('10', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:18:27', '0', '272', '1.4Nacos配置入门');
INSERT INTO `ol_learn_record` VALUES ('11', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:41:08', '0', '275', '2.1什么是服务发现');
INSERT INTO `ol_learn_record` VALUES ('12', '117', 'Nacos微服务开发实战', '52', '2022-10-06 13:18:46', '0', '276', '2.2服务发现快速入门');
