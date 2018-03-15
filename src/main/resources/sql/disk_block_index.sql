/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : BlockChain

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2018-03-12 17:47:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for disk_block_index
-- ----------------------------
DROP TABLE IF EXISTS `disk_block_index`;
CREATE TABLE `disk_block_index` (
  `block_hash` varchar(255) DEFAULT NULL,
  `n_file` int(11) DEFAULT NULL,
  `n_block_pos` int(11) DEFAULT NULL,
  `n_height` int(11) DEFAULT NULL,
  `next_hash` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `prev_hash` varchar(255) DEFAULT NULL,
  `merkle_hash` varchar(255) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `bits` bigint(20) DEFAULT NULL,
  `nonce` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
