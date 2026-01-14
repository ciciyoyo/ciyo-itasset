CREATE TABLE `itam_device_monthly_stats`
(
    `id`                       bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `assets_id`                bigint(20) NOT NULL COMMENT '资产 ID',
    `category_id`              bigint(20) DEFAULT NULL COMMENT '分类 ID',
    `model_id`                 bigint(20) DEFAULT NULL COMMENT '型号 ID',
    `location_id`              bigint(20) DEFAULT NULL COMMENT '位置 ID',
    `depreciation_id`          bigint(20) DEFAULT NULL COMMENT '折旧规则 ID',
    `initial_value`            decimal(20, 6) DEFAULT NULL COMMENT '初始价值(原值)',
    `current_value`            decimal(20, 6) DEFAULT NULL COMMENT '当前价值',
    `accumulated_depreciation` decimal(20, 6) DEFAULT NULL COMMENT '累计折旧',
    `stats_month`              varchar(7) NOT NULL COMMENT '统计月份 (YYYY-MM)',
    `stats_date`               date           DEFAULT NULL COMMENT '统计日期',
    `tenant_id`                bigint(20) DEFAULT NULL COMMENT '租户 ID',
    `create_time`              datetime       DEFAULT NULL COMMENT '创建时间',
    `update_time`              datetime       DEFAULT NULL COMMENT '更新时间',
    `create_by`                bigint(20) DEFAULT NULL COMMENT '创建人',
    `update_by`                bigint(20) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assets_month` (`assets_id`,`stats_month`) USING BTREE,
    KEY                        `idx_stats_month` (`stats_month`),
    KEY                        `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产月度价值统计表';
