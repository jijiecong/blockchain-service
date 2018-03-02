/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : bitcoin

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2018-03-01 16:23:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for block
-- ----------------------------
DROP TABLE IF EXISTS `block`;
CREATE TABLE `block` (
  `block_hash` varchar(64) NOT NULL,
  `bits` bigint(20) NOT NULL,
  `height` bigint(20) NOT NULL,
  `merkle_hash` varchar(64) NOT NULL,
  `nonce` bigint(20) NOT NULL,
  `prev_hash` varchar(64) NOT NULL,
  `size` bigint(20) NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  `tx_count` bigint(20) NOT NULL,
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`block_hash`),
  UNIQUE KEY `uni_prev_hash` (`prev_hash`),
  UNIQUE KEY `uni_height` (`height`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
