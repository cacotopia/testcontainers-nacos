/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS nacos;

-- Set search path
SET search_path TO nacos;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS config_info;
CREATE TABLE config_info (
  id SERIAL PRIMARY KEY,
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(255),
  content TEXT NOT NULL,
  md5 VARCHAR(32),
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  src_user TEXT,
  src_ip VARCHAR(50),
  app_name VARCHAR(128),
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段',
  c_desc VARCHAR(256),
  c_use VARCHAR(64),
  effect VARCHAR(64),
  type VARCHAR(64),
  c_schema TEXT
);

CREATE UNIQUE INDEX uk_configinfo_datagrouptenant ON config_info (data_id, group_id, tenant_id);

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS config_info_aggr;
CREATE TABLE config_info_aggr (
  id SERIAL PRIMARY KEY,
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(255) NOT NULL,
  datum_id VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  gmt_modified TIMESTAMP NOT NULL,
  app_name VARCHAR(128),
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段'
);

CREATE UNIQUE INDEX uk_configinfoaggr_datagrouptenantdatum ON config_info_aggr (data_id, group_id, tenant_id, datum_id);

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS config_info_beta;
CREATE TABLE config_info_beta (
  id SERIAL PRIMARY KEY,
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(128) NOT NULL,
  app_name VARCHAR(128),
  content TEXT NOT NULL,
  beta_ips VARCHAR(1024),
  md5 VARCHAR(32),
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  src_user TEXT,
  src_ip VARCHAR(50),
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段'
);

CREATE UNIQUE INDEX uk_configinfobeta_datagrouptenant ON config_info_beta (data_id, group_id, tenant_id);

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS config_info_tag;
CREATE TABLE config_info_tag (
  id SERIAL PRIMARY KEY,
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(128) NOT NULL,
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段',
  tag_id VARCHAR(128) NOT NULL,
  app_name VARCHAR(128),
  content TEXT NOT NULL,
  md5 VARCHAR(32),
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  src_user TEXT,
  src_ip VARCHAR(50)
);

CREATE UNIQUE INDEX uk_configinfotag_datagrouptenanttag ON config_info_tag (data_id, group_id, tenant_id, tag_id);

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS config_tags_relation;
CREATE TABLE config_tags_relation (
  id BIGINT NOT NULL,
  tag_name VARCHAR(128) NOT NULL,
  tag_type VARCHAR(64),
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(128) NOT NULL,
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段',
  nid SERIAL PRIMARY KEY
);

CREATE UNIQUE INDEX uk_configtagrelation_configidtag ON config_tags_relation (id, tag_name, tag_type);
CREATE INDEX idx_tenant_id ON config_tags_relation (tenant_id);

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS group_capacity;
CREATE TABLE group_capacity (
  id SERIAL PRIMARY KEY,
  group_id VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  quota INTEGER NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  usage INTEGER NOT NULL DEFAULT 0 COMMENT '使用量',
  max_size INTEGER NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  max_aggr_count INTEGER NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，0表示使用默认值',
  max_aggr_size INTEGER NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  max_history_count INTEGER NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'
);

CREATE UNIQUE INDEX uk_group_id ON group_capacity (group_id);

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS his_config_info;
CREATE TABLE his_config_info (
  id BIGINT NOT NULL,
  nid SERIAL PRIMARY KEY,
  data_id VARCHAR(255) NOT NULL,
  group_id VARCHAR(128) NOT NULL,
  app_name VARCHAR(128),
  content TEXT NOT NULL,
  md5 VARCHAR(32),
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  src_user TEXT,
  src_ip VARCHAR(50),
  op_type VARCHAR(10),
  tenant_id VARCHAR(128) DEFAULT '' COMMENT '租户字段'
);

CREATE INDEX idx_gmt_create ON his_config_info (gmt_create);
CREATE INDEX idx_gmt_modified ON his_config_info (gmt_modified);
CREATE INDEX idx_did ON his_config_info (data_id);

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS tenant_capacity;
CREATE TABLE tenant_capacity (
  id SERIAL PRIMARY KEY,
  tenant_id VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  quota INTEGER NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  usage INTEGER NOT NULL DEFAULT 0 COMMENT '使用量',
  max_size INTEGER NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  max_aggr_count INTEGER NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  max_aggr_size INTEGER NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  max_history_count INTEGER NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  gmt_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  gmt_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'
);

CREATE UNIQUE INDEX uk_tenant_id ON tenant_capacity (tenant_id);

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS tenant_info;
CREATE TABLE tenant_info (
  id SERIAL PRIMARY KEY,
  kp VARCHAR(128) NOT NULL,
  tenant_id VARCHAR(128) DEFAULT '' COMMENT 'tenant_id',
  tenant_name VARCHAR(128) DEFAULT '' COMMENT 'tenant_name',
  tenant_desc VARCHAR(256) DEFAULT NULL COMMENT 'tenant_desc',
  create_source VARCHAR(32) DEFAULT NULL COMMENT 'create_source',
  gmt_create BIGINT NOT NULL COMMENT '创建时间',
  gmt_modified BIGINT NOT NULL COMMENT '修改时间'
);

CREATE UNIQUE INDEX uk_tenant_info_kptenantid ON tenant_info (kp, tenant_id);
CREATE INDEX idx_tenant_id ON tenant_info (tenant_id);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
  username VARCHAR(50) PRIMARY KEY,
  password VARCHAR(500) NOT NULL,
  enabled BOOLEAN NOT NULL
);

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
  username VARCHAR(50) NOT NULL,
  role VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX idx_user_role ON roles (username, role);

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS permissions;
CREATE TABLE permissions (
  role VARCHAR(50) NOT NULL,
  resource VARCHAR(255) NOT NULL,
  action VARCHAR(8) NOT NULL
);

CREATE UNIQUE INDEX uk_role_permission ON permissions (role, resource, action);

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO config_info (data_id, group_id, content, md5, app_name) VALUES ('nacos.core.version', 'DEFAULT_GROUP', '2.2.3', '61e0148c53f84669c0f42d35154be843', 'nacos');

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');
