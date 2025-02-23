/*
 Navicat Premium Dump SQL

 Source Server         : docker-mysql8
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : 192.168.118.128:3306
 Source Schema         : test_database

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 23/02/2025 22:15:25
*/

CREATE DATABASE `test_database` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT ENCRYPTION='N'

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for EXPORT_TASK
-- ----------------------------
DROP TABLE IF EXISTS `EXPORT_TASK`;
CREATE TABLE `EXPORT_TASK`  (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `MODULE` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块名',
  `FILE_NAME` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `FILE_PATH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件路径',
  `STATUS` int NULL DEFAULT NULL COMMENT '状态，0：未开始，1：开始，2：成功，3：失败',
  `USER_NAME` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `QUERY` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'sql语句',
  `CREATE_TIME` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `START_TIME` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `END_TIME` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `TIME_COST` int NULL DEFAULT NULL COMMENT '总时间消费',
  `REASON` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原因',
  `DB_START_DATE` datetime NULL DEFAULT NULL COMMENT 'db开始时间',
  `DB_END_DATE` datetime NULL DEFAULT NULL COMMENT 'db结束时间',
  `DB_TIME_COST` int NULL DEFAULT NULL COMMENT 'db时间消费',
  `EXCEL_WRITE_START_DATE` datetime NULL DEFAULT NULL COMMENT 'excel写开始时间',
  `EXCEL_WRITE_END_DATE` datetime NULL DEFAULT NULL COMMENT 'excel写结束时间',
  `EXCEL_WRITE_TIME_COST` int NULL DEFAULT NULL COMMENT 'excel写时间消费',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for EXPORT_TASK_BAK
-- ----------------------------
DROP TABLE IF EXISTS `EXPORT_TASK_BAK`;
CREATE TABLE `EXPORT_TASK_BAK`  (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `MODULE` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块名',
  `FILE_NAME` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
  `FILE_PATH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件路径',
  `STATUS` int NULL DEFAULT NULL COMMENT '状态，0：未开始，1：开始，2：成功，3：失败',
  `USER_NAME` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `QUERY` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'sql语句',
  `CREATE_TIME` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `START_TIME` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `END_TIME` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `TIME_COST` int NULL DEFAULT NULL COMMENT '总时间消费',
  `REASON` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原因',
  `DB_START_DATE` datetime NULL DEFAULT NULL COMMENT 'db开始时间',
  `DB_END_DATE` datetime NULL DEFAULT NULL COMMENT 'db结束时间',
  `DB_TIME_COST` int NULL DEFAULT NULL COMMENT 'db时间消费',
  `EXCEL_WRITE_START_DATE` datetime NULL DEFAULT NULL COMMENT 'excel写开始时间',
  `EXCEL_WRITE_END_DATE` datetime NULL DEFAULT NULL COMMENT 'excel写结束时间',
  `EXCEL_WRITE_TIME_COST` int NULL DEFAULT NULL COMMENT 'excel写时间消费',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for USER
-- ----------------------------
DROP TABLE IF EXISTS `USER`;
CREATE TABLE `USER`  (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1500001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Procedure structure for addTestData
-- ----------------------------
DROP PROCEDURE IF EXISTS `addTestData`;
delimiter ;;
CREATE PROCEDURE `addTestData`()
begin
declare number int;
set number = 1;
while number <= 1500000 -- 插入N条数据
do
insert into USER(ID,NAME)
values(number, concat('用户_',number)); -- 为了区分姓名，我们加上后缀
set number = number + 1;
end
while;
end
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

-- 执行 addTestData 输入语句  CALL addTestData