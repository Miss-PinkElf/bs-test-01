CREATE DATABASE IF NOT EXISTS grain_env_predict
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE grain_env_predict;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS prediction_result;
DROP TABLE IF EXISTS prediction_task;
DROP TABLE IF EXISTS grain_temp_summary;
DROP TABLE IF EXISTS grain_temp_record;
DROP TABLE IF EXISTS grain_temp_point;
DROP TABLE IF EXISTS sensor_data;
DROP TABLE IF EXISTS sensor_metric;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS warehouse;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_code VARCHAR(64) NOT NULL COMMENT '仓库编号',
    warehouse_name VARCHAR(128) NOT NULL COMMENT '仓库名称',
    location VARCHAR(255) NOT NULL COMMENT '仓库位置',
    capacity_ton DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '容量（吨）',
    manager_name VARCHAR(64) NOT NULL COMMENT '负责人姓名',
    contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '仓库状态：ACTIVE/WARNING/MAINTENANCE/DISABLED',
    grain_type VARCHAR(64) DEFAULT NULL COMMENT '主要粮食品类',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_warehouse_code (warehouse_code),
    KEY idx_warehouse_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮仓基础信息表';

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '登录用户名',
    password VARCHAR(128) NOT NULL COMMENT '登录密码或密码摘要',
    display_name VARCHAR(64) NOT NULL COMMENT '展示名称',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    warehouse_id BIGINT DEFAULT NULL COMMENT '所属仓库，为空表示平台级账号',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE/DISABLED',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_user_status (status),
    KEY idx_user_warehouse (warehouse_id),
    CONSTRAINT fk_user_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE sensor_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码',
    metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    unit VARCHAR(32) NOT NULL COMMENT '单位',
    min_threshold DECIMAL(10, 2) DEFAULT NULL COMMENT '最小阈值',
    max_threshold DECIMAL(10, 2) DEFAULT NULL COMMENT '最大阈值',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境指标定义表';

CREATE TABLE sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码，仅用于湿度/二氧化碳等普通单值指标',
    metric_value DECIMAL(10, 2) NOT NULL COMMENT '指标值',
    collected_at DATETIME NOT NULL COMMENT '采集时间',
    source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '来源：MANUAL/IMPORT',
    source_batch_no VARCHAR(64) DEFAULT NULL COMMENT '导入批次号',
    quality_flag VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '质量标记：NORMAL/ABNORMAL/CLEANED',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_sensor_query (warehouse_id, metric_code, collected_at),
    KEY idx_sensor_batch (source_batch_no),
    KEY idx_sensor_created_by (created_by),
    CONSTRAINT fk_sensor_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    CONSTRAINT fk_sensor_metric FOREIGN KEY (metric_code) REFERENCES sensor_metric (metric_code),
    CONSTRAINT fk_sensor_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通环境监测数据表，主要保存湿度和二氧化碳等单值指标';

CREATE TABLE grain_temp_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
    probe_code VARCHAR(64) NOT NULL COMMENT '测温缆编号',
    zone_code VARCHAR(32) NOT NULL COMMENT '区域编号，如A区/B区',
    layer_no INT NOT NULL COMMENT '层号',
    point_no INT NOT NULL COMMENT '点位号',
    point_name VARCHAR(128) NOT NULL COMMENT '测点名称',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_grain_temp_point (warehouse_id, zone_code, layer_no, point_no),
    KEY idx_grain_temp_point_probe (warehouse_id, probe_code),
    CONSTRAINT fk_grain_temp_point_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮温测点定义表';

CREATE TABLE grain_temp_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
    point_id BIGINT NOT NULL COMMENT '测点ID',
    collected_at DATETIME NOT NULL COMMENT '检测时间',
    temperature_value DECIMAL(10, 2) NOT NULL COMMENT '温度值',
    source_type VARCHAR(32) NOT NULL DEFAULT 'IMPORT' COMMENT '来源：IMPORT/MANUAL',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    quality_flag VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '质量标记',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '录入人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_grain_temp_record_point_time (point_id, collected_at),
    KEY idx_grain_temp_record_query (warehouse_id, collected_at),
    CONSTRAINT fk_grain_temp_record_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    CONSTRAINT fk_grain_temp_record_point FOREIGN KEY (point_id) REFERENCES grain_temp_point (id),
    CONSTRAINT fk_grain_temp_record_user FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮温原始测点记录表';

CREATE TABLE grain_temp_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
    collected_at DATETIME NOT NULL COMMENT '检测时间',
    avg_temp DECIMAL(10, 2) NOT NULL COMMENT '整仓平均温度',
    max_temp DECIMAL(10, 2) NOT NULL COMMENT '最高温',
    min_temp DECIMAL(10, 2) NOT NULL COMMENT '最低温',
    layer_1_avg DECIMAL(10, 2) DEFAULT NULL COMMENT '第一层平均温度',
    layer_2_avg DECIMAL(10, 2) DEFAULT NULL COMMENT '第二层平均温度',
    layer_3_avg DECIMAL(10, 2) DEFAULT NULL COMMENT '第三层平均温度',
    layer_4_avg DECIMAL(10, 2) DEFAULT NULL COMMENT '第四层平均温度',
    warning_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '真实预警等级：NORMAL/ATTENTION/WARNING',
    warning_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否预警：0否1是',
    warning_message VARCHAR(255) DEFAULT NULL COMMENT '预警说明',
    analysis_result VARCHAR(128) DEFAULT NULL COMMENT '分析结果，如粮温正常',
    analysis_remark VARCHAR(255) DEFAULT NULL COMMENT '分析备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_grain_temp_summary_time (warehouse_id, collected_at),
    KEY idx_grain_temp_summary_query (warehouse_id, collected_at),
    CONSTRAINT fk_grain_temp_summary_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮温汇总分析表';

CREATE TABLE prediction_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_no VARCHAR(64) NOT NULL COMMENT '预测任务编号',
    parent_task_id BIGINT DEFAULT NULL COMMENT '父任务ID，指向上一轮预测',
    task_round INT NOT NULL DEFAULT 1 COMMENT '预测轮次',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    metric_code VARCHAR(64) NOT NULL COMMENT '预测指标编码',
    data_source_type VARCHAR(64) NOT NULL COMMENT '数据来源类型，如GRAIN_TEMP_SUMMARY',
    target_type VARCHAR(64) NOT NULL COMMENT '预测目标类型，如AVG_TEMP/LAYER_1_AVG',
    algorithm_code VARCHAR(64) NOT NULL COMMENT '算法编码',
    algorithm_name VARCHAR(64) NOT NULL COMMENT '算法名称',
    train_start_time DATETIME NOT NULL COMMENT '训练开始时间',
    train_end_time DATETIME NOT NULL COMMENT '训练结束时间',
    forecast_start_time DATETIME NOT NULL COMMENT '预测开始时间',
    forecast_end_time DATETIME NOT NULL COMMENT '预测结束时间',
    based_on_actual_end_time DATETIME DEFAULT NULL COMMENT '已纳入真实数据截止时间',
    forecast_days INT NOT NULL DEFAULT 0 COMMENT '预测天数',
    sample_size INT NOT NULL DEFAULT 0 COMMENT '参与计算的样本数',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'INITIAL' COMMENT '触发类型：INITIAL/CORRECTION/ROLLING',
    adjust_status VARCHAR(32) NOT NULL DEFAULT 'UNADJUSTED' COMMENT '修正状态：UNADJUSTED/ADJUSTED',
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '任务状态：SUCCESS/FAILED',
    risk_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '整体风险等级：NORMAL/ATTENTION/WARNING',
    requested_by BIGINT DEFAULT NULL COMMENT '发起人',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    summary VARCHAR(255) DEFAULT NULL COMMENT '任务摘要',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_prediction_task_no (task_no),
    KEY idx_prediction_task_query (warehouse_id, metric_code, requested_at),
    KEY idx_prediction_task_parent (parent_task_id),
    CONSTRAINT fk_prediction_task_parent FOREIGN KEY (parent_task_id) REFERENCES prediction_task (id),
    CONSTRAINT fk_prediction_task_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    CONSTRAINT fk_prediction_task_metric FOREIGN KEY (metric_code) REFERENCES sensor_metric (metric_code),
    CONSTRAINT fk_prediction_task_user FOREIGN KEY (requested_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='滚动预测任务主表';

CREATE TABLE prediction_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '预测任务ID',
    phase_type VARCHAR(32) NOT NULL COMMENT '结果阶段：VERIFY/FUTURE',
    step_index INT NOT NULL COMMENT '步序号',
    result_time DATETIME NOT NULL COMMENT '结果对应时间',
    actual_value DECIMAL(10, 2) DEFAULT NULL COMMENT '真实值',
    predicted_value DECIMAL(10, 2) NOT NULL COMMENT '预测值',
    error_value DECIMAL(10, 2) DEFAULT NULL COMMENT '误差值，通常为真实值减预测值',
    error_rate DECIMAL(10, 2) DEFAULT NULL COMMENT '误差率，单位百分比',
    warning_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '预测预警等级：NORMAL/ATTENTION/WARNING',
    warning_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否预警：0否1是',
    warning_message VARCHAR(255) DEFAULT NULL COMMENT '预警说明',
    is_corrected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否属于修正后的结果',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_prediction_result_task_time (task_id, result_time),
    KEY idx_prediction_result_time (result_time),
    CONSTRAINT fk_prediction_result_task FOREIGN KEY (task_id) REFERENCES prediction_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='滚动预测结果表';

INSERT INTO warehouse (id, warehouse_code, warehouse_name, location, capacity_ton, manager_name, contact_phone, status, grain_type, remark)
VALUES
    (1, 'WH-A01', '一号平房仓', '东区 1 号库', 1200.00, '张建国', '13800000001', 'ACTIVE', '小麦', '稳定仓样本'),
    (2, 'WH-A02', '二号平房仓', '东区 2 号库', 980.00, '李春华', '13800000002', 'WARNING', '玉米', '升温风险仓样本'),
    (3, 'WH-B01', '三号立筒仓', '西区 1 号库', 1500.00, '王利民', '13800000003', 'MAINTENANCE', '稻谷', '维护中'),
    (4, 'WH-B02', '四号平房仓', '西区 2 号库', 1100.00, '赵志勇', '13800000004', 'ACTIVE', '大豆', '普通演示仓库'),
    (5, 'WH-C01', '五号浅圆仓', '南区 1 号库', 860.00, '孙海燕', '13800000005', 'ACTIVE', '小麦', '普通演示仓库'),
    (6, 'WH-C02', '六号立筒仓', '南区 2 号库', 1320.00, '周广林', '13800000006', 'ACTIVE', '玉米', '对比仓样本');

INSERT INTO sys_role (id, role_code, role_name, role_desc)
VALUES
    (1, 'ADMIN', '管理员', '管理用户、仓库、全部环境数据和预测记录'),
    (2, 'WAREHOUSE_MANAGER', '仓库管理员', '维护所属仓库环境数据并执行预测'),
    (3, 'VIEWER', '查看者', '只读查看仪表盘、图表和预测结果');

INSERT INTO sys_user (id, username, password, display_name, phone, warehouse_id, status)
VALUES
    (1, 'admin', '123456', '系统管理员', '13800001000', NULL, 'ACTIVE'),
    (2, 'manager_a01', '123456', '东一区管理员', '13800001001', 1, 'ACTIVE'),
    (3, 'viewer_demo', '123456', '演示查看者', '13800001002', NULL, 'ACTIVE'),
    (4, 'manager_a02', '123456', '东二区管理员', '13800001003', 2, 'ACTIVE'),
    (5, 'manager_b02', '123456', '西二区管理员', '13800001004', 4, 'ACTIVE'),
    (6, 'manager_c02', '123456', '南二区管理员', '13800001005', 6, 'ACTIVE');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 2),
    (5, 2),
    (6, 2);

INSERT INTO sensor_metric (id, metric_code, metric_name, unit, min_threshold, max_threshold, status)
VALUES
    (1, 'temperature', '温度', '°C', 5.00, 28.00, 'ACTIVE'),
    (2, 'humidity', '湿度', '%', 30.00, 75.00, 'ACTIVE'),
    (3, 'co2', '二氧化碳浓度', 'ppm', 300.00, 1500.00, 'ACTIVE');

INSERT INTO sensor_data (
    warehouse_id,
    metric_code,
    metric_value,
    collected_at,
    source_type,
    source_batch_no,
    quality_flag,
    remark,
    created_by
)
SELECT
    warehouse_cfg.warehouse_id,
    metric_cfg.metric_code,
    ROUND(
        CASE metric_cfg.metric_code
            WHEN 'humidity' THEN
                CASE warehouse_cfg.warehouse_id
                    WHEN 1 THEN 53.20 + day.day_offset * 0.045 + (MOD(day.day_offset, 6) - 2) * 0.18
                    WHEN 2 THEN 56.10 + day.day_offset * 0.070 + (MOD(day.day_offset, 5) - 2) * 0.24
                    ELSE 54.80 + day.day_offset * 0.055 + (MOD(day.day_offset, 8) - 4) * 0.16
                END
            WHEN 'co2' THEN
                CASE warehouse_cfg.warehouse_id
                    WHEN 1 THEN 515.00 + day.day_offset * 1.25 + (MOD(day.day_offset, 7) - 3) * 6
                    WHEN 2 THEN 620.00 + day.day_offset * 2.10 + (MOD(day.day_offset, 6) - 3) * 9
                    ELSE 560.00 + day.day_offset * 1.65 + (MOD(day.day_offset, 10) - 5) * 7
                END
        END,
        2
    ) AS metric_value,
    day.collected_at_sensor,
    'IMPORT',
    CONCAT('PHASE11-SENSOR-', warehouse_cfg.story_code),
    'NORMAL',
    CASE metric_cfg.metric_code
        WHEN 'humidity' THEN CONCAT(warehouse_cfg.story_name, '河南春季湿度基线')
        ELSE CONCAT(warehouse_cfg.story_name, '通风二氧化碳基线')
    END,
    warehouse_cfg.created_by
FROM (
    SELECT
        day_offset,
        collected_at_sensor
    FROM (
        SELECT
            seq.n AS day_offset,
            DATE_ADD('2025-01-01 08:00:00', INTERVAL seq.n DAY) AS collected_at_sensor
        FROM (
            SELECT ones.n + tens.n * 10 + hundreds.n * 100 AS n
            FROM (
                SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
            ) ones
            CROSS JOIN (
                SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
            ) tens
            CROSS JOIN (
                SELECT 0 AS n UNION ALL SELECT 1
            ) hundreds
        ) seq
        WHERE seq.n < 120
    ) phase11_sensor_days
) day
CROSS JOIN (
    SELECT 1 AS warehouse_id, 2 AS created_by, 'stable' AS story_code, '稳定仓' AS story_name
    UNION ALL
    SELECT 2, 4, 'risk', '风险仓'
    UNION ALL
    SELECT 6, 6, 'contrast', '对比仓'
) warehouse_cfg
CROSS JOIN (
    SELECT 'humidity' AS metric_code
    UNION ALL
    SELECT 'co2'
) metric_cfg
ORDER BY warehouse_cfg.warehouse_id, metric_cfg.metric_code, day.collected_at_sensor;

INSERT INTO grain_temp_point (id, warehouse_id, probe_code, zone_code, layer_no, point_no, point_name, status, remark)
VALUES
    (1, 1, 'CABLE-A', 'A', 1, 1, 'A-1-1测点', 'ACTIVE', NULL),
    (2, 1, 'CABLE-A', 'A', 1, 2, 'A-1-2测点', 'ACTIVE', NULL),
    (3, 1, 'CABLE-A', 'A', 1, 3, 'A-1-3测点', 'ACTIVE', NULL),
    (4, 1, 'CABLE-A', 'A', 1, 4, 'A-1-4测点', 'ACTIVE', NULL),
    (5, 1, 'CABLE-A', 'A', 2, 1, 'A-2-1测点', 'ACTIVE', NULL),
    (6, 1, 'CABLE-A', 'A', 2, 2, 'A-2-2测点', 'ACTIVE', NULL),
    (7, 1, 'CABLE-A', 'A', 2, 3, 'A-2-3测点', 'ACTIVE', NULL),
    (8, 1, 'CABLE-A', 'A', 2, 4, 'A-2-4测点', 'ACTIVE', NULL),
    (9, 1, 'CABLE-A', 'A', 3, 1, 'A-3-1测点', 'ACTIVE', NULL),
    (10, 1, 'CABLE-A', 'A', 3, 2, 'A-3-2测点', 'ACTIVE', NULL),
    (11, 1, 'CABLE-A', 'A', 3, 3, 'A-3-3测点', 'ACTIVE', NULL),
    (12, 1, 'CABLE-A', 'A', 3, 4, 'A-3-4测点', 'ACTIVE', NULL),
    (13, 1, 'CABLE-A', 'A', 4, 1, 'A-4-1测点', 'ACTIVE', NULL),
    (14, 1, 'CABLE-A', 'A', 4, 2, 'A-4-2测点', 'ACTIVE', NULL),
    (15, 1, 'CABLE-A', 'A', 4, 3, 'A-4-3测点', 'ACTIVE', NULL),
    (16, 1, 'CABLE-A', 'A', 4, 4, 'A-4-4测点', 'ACTIVE', NULL),
    (17, 2, 'CABLE-B', 'A', 1, 1, 'A-1-1测点', 'ACTIVE', NULL),
    (18, 2, 'CABLE-B', 'A', 1, 2, 'A-1-2测点', 'ACTIVE', NULL),
    (19, 2, 'CABLE-B', 'A', 1, 3, 'A-1-3测点', 'ACTIVE', NULL),
    (20, 2, 'CABLE-B', 'A', 1, 4, 'A-1-4测点', 'ACTIVE', NULL),
    (21, 2, 'CABLE-B', 'A', 2, 1, 'A-2-1测点', 'ACTIVE', NULL),
    (22, 2, 'CABLE-B', 'A', 2, 2, 'A-2-2测点', 'ACTIVE', NULL),
    (23, 2, 'CABLE-B', 'A', 2, 3, 'A-2-3测点', 'ACTIVE', NULL),
    (24, 2, 'CABLE-B', 'A', 2, 4, 'A-2-4测点', 'ACTIVE', NULL),
    (25, 2, 'CABLE-B', 'A', 3, 1, 'A-3-1测点', 'ACTIVE', NULL),
    (26, 2, 'CABLE-B', 'A', 3, 2, 'A-3-2测点', 'ACTIVE', NULL),
    (27, 2, 'CABLE-B', 'A', 3, 3, 'A-3-3测点', 'ACTIVE', NULL),
    (28, 2, 'CABLE-B', 'A', 3, 4, 'A-3-4测点', 'ACTIVE', NULL),
    (29, 2, 'CABLE-B', 'A', 4, 1, 'A-4-1测点', 'ACTIVE', NULL),
    (30, 2, 'CABLE-B', 'A', 4, 2, 'A-4-2测点', 'ACTIVE', NULL),
    (31, 2, 'CABLE-B', 'A', 4, 3, 'A-4-3测点', 'ACTIVE', NULL),
    (32, 2, 'CABLE-B', 'A', 4, 4, 'A-4-4测点', 'ACTIVE', NULL),
    (33, 6, 'CABLE-C', 'B', 1, 1, 'B-1-1测点', 'ACTIVE', NULL),
    (34, 6, 'CABLE-C', 'B', 1, 2, 'B-1-2测点', 'ACTIVE', NULL),
    (35, 6, 'CABLE-C', 'B', 1, 3, 'B-1-3测点', 'ACTIVE', NULL),
    (36, 6, 'CABLE-C', 'B', 1, 4, 'B-1-4测点', 'ACTIVE', NULL),
    (37, 6, 'CABLE-C', 'B', 2, 1, 'B-2-1测点', 'ACTIVE', NULL),
    (38, 6, 'CABLE-C', 'B', 2, 2, 'B-2-2测点', 'ACTIVE', NULL),
    (39, 6, 'CABLE-C', 'B', 2, 3, 'B-2-3测点', 'ACTIVE', NULL),
    (40, 6, 'CABLE-C', 'B', 2, 4, 'B-2-4测点', 'ACTIVE', NULL),
    (41, 6, 'CABLE-C', 'B', 3, 1, 'B-3-1测点', 'ACTIVE', NULL),
    (42, 6, 'CABLE-C', 'B', 3, 2, 'B-3-2测点', 'ACTIVE', NULL),
    (43, 6, 'CABLE-C', 'B', 3, 3, 'B-3-3测点', 'ACTIVE', NULL),
    (44, 6, 'CABLE-C', 'B', 3, 4, 'B-3-4测点', 'ACTIVE', NULL),
    (45, 6, 'CABLE-C', 'B', 4, 1, 'B-4-1测点', 'ACTIVE', NULL),
    (46, 6, 'CABLE-C', 'B', 4, 2, 'B-4-2测点', 'ACTIVE', NULL),
    (47, 6, 'CABLE-C', 'B', 4, 3, 'B-4-3测点', 'ACTIVE', NULL),
    (48, 6, 'CABLE-C', 'B', 4, 4, 'B-4-4测点', 'ACTIVE', NULL);

INSERT INTO grain_temp_record (
    warehouse_id,
    point_id,
    collected_at,
    temperature_value,
    source_type,
    batch_no,
    quality_flag,
    remark,
    created_by
)
SELECT
    point.warehouse_id,
    point.id,
    day.collected_at_grain,
    ROUND(
        CASE point.warehouse_id
            WHEN 1 THEN 16.20 + day.day_offset * 0.034 + (point.layer_no - 2.5) * 0.20 + (point.point_no - 2.5) * 0.06
                + (MOD(day.day_offset, 7) - 3) * 0.03
            WHEN 2 THEN 19.00 + day.day_offset * 0.060 + (point.layer_no - 2.5) * 0.36 + (point.point_no - 2.5) * 0.08
                + (MOD(day.day_offset, 6) - 3) * 0.05
            ELSE 17.20 + day.day_offset * 0.050 + (point.layer_no - 2.5) * 0.25 + (point.point_no - 2.5) * 0.10
                + CASE
                    WHEN MOD(day.day_offset, 9) IN (0, 1, 2) THEN 0.14
                    WHEN MOD(day.day_offset, 9) IN (6, 7, 8) THEN -0.10
                    ELSE 0.04
                END
        END,
        2
    ) AS temperature_value,
    'IMPORT',
    CASE point.warehouse_id
        WHEN 1 THEN 'PHASE11-GRAIN-STABLE'
        WHEN 2 THEN 'PHASE11-GRAIN-RISK'
        ELSE 'PHASE11-GRAIN-CONTRAST'
    END,
    'NORMAL',
    CASE point.warehouse_id
        WHEN 1 THEN '稳定仓河南 1-4 月粮温基线'
        WHEN 2 THEN '风险仓河南 1-4 月粮温基线'
        ELSE '对比仓河南 1-4 月粮温基线'
    END,
    CASE point.warehouse_id
        WHEN 1 THEN 2
        WHEN 2 THEN 4
        ELSE 6
    END
FROM (
    SELECT
        seq.n AS day_offset,
        DATE_ADD('2025-01-01 08:40:00', INTERVAL seq.n DAY) AS collected_at_grain
    FROM (
        SELECT ones.n + tens.n * 10 + hundreds.n * 100 AS n
        FROM (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) ones
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) tens
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1
        ) hundreds
    ) seq
    WHERE seq.n < 120
) day
JOIN grain_temp_point point ON point.warehouse_id IN (1, 2, 6)
ORDER BY point.warehouse_id, day.day_offset, point.id;

INSERT INTO grain_temp_summary (
    warehouse_id,
    collected_at,
    avg_temp,
    max_temp,
    min_temp,
    layer_1_avg,
    layer_2_avg,
    layer_3_avg,
    layer_4_avg,
    warning_level,
    warning_flag,
    warning_message,
    analysis_result,
    analysis_remark
)
SELECT
    record.warehouse_id,
    record.collected_at,
    ROUND(AVG(record.temperature_value), 2) AS avg_temp,
    ROUND(MAX(record.temperature_value), 2) AS max_temp,
    ROUND(MIN(record.temperature_value), 2) AS min_temp,
    ROUND(AVG(CASE WHEN point.layer_no = 1 THEN record.temperature_value END), 2) AS layer_1_avg,
    ROUND(AVG(CASE WHEN point.layer_no = 2 THEN record.temperature_value END), 2) AS layer_2_avg,
    ROUND(AVG(CASE WHEN point.layer_no = 3 THEN record.temperature_value END), 2) AS layer_3_avg,
    ROUND(AVG(CASE WHEN point.layer_no = 4 THEN record.temperature_value END), 2) AS layer_4_avg,
    CASE
        WHEN record.warehouse_id = 2 AND MAX(record.temperature_value) >= 26.70 THEN 'ATTENTION'
        WHEN record.warehouse_id = 6 AND record.collected_at >= '2025-04-20 08:40:00' AND MAX(record.temperature_value) >= 23.80 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END AS warning_level,
    CASE
        WHEN record.warehouse_id = 2 AND MAX(record.temperature_value) >= 26.70 THEN 1
        WHEN record.warehouse_id = 6 AND record.collected_at >= '2025-04-20 08:40:00' AND MAX(record.temperature_value) >= 23.80 THEN 1
        ELSE 0
    END AS warning_flag,
    CASE
        WHEN record.warehouse_id = 2 AND MAX(record.temperature_value) >= 26.70 THEN '四月末仓温逼近阈值，建议答辩时强调提前通风与重点巡检。'
        WHEN record.warehouse_id = 6 AND record.collected_at >= '2025-04-20 08:40:00' AND MAX(record.temperature_value) >= 23.80 THEN '对比仓出现轻微抬升，可用于说明波动仓与风险仓的差异。'
        ELSE NULL
    END AS warning_message,
    CASE
        WHEN record.warehouse_id = 2 AND MAX(record.temperature_value) >= 26.70 THEN '粮温关注'
        WHEN record.warehouse_id = 6 AND record.collected_at >= '2025-04-20 08:40:00' AND MAX(record.temperature_value) >= 23.80 THEN '轻微波动'
        ELSE '粮温正常'
    END AS analysis_result,
    CASE record.warehouse_id
        WHEN 1 THEN '稳定仓：1-4 月温升平缓，适合展示低风险基线。'
        WHEN 2 THEN '风险仓：进入 4 月后升温更快，用于展示接近阈值的预警故事线。'
        ELSE '对比仓：保留一定波动，但不再沿用旧 9 月高温修正叙事。'
    END AS analysis_remark
FROM grain_temp_record record
JOIN grain_temp_point point ON point.id = record.point_id
GROUP BY record.warehouse_id, record.collected_at
ORDER BY record.warehouse_id, record.collected_at;

INSERT INTO prediction_task (
    task_no,
    parent_task_id,
    task_round,
    warehouse_id,
    metric_code,
    data_source_type,
    target_type,
    algorithm_code,
    algorithm_name,
    train_start_time,
    train_end_time,
    forecast_start_time,
    forecast_end_time,
    based_on_actual_end_time,
    forecast_days,
    sample_size,
    trigger_type,
    adjust_status,
    status,
    risk_level,
    requested_by,
    requested_at,
    completed_at,
    summary,
    remark
)
VALUES
    (
        'TASK-PHASE11-RISK-001',
        NULL,
        1,
        2,
        'temperature',
        'GRAIN_TEMP_SUMMARY',
        'AVG_TEMP',
        'WEIGHTED_MOVING_AVERAGE',
        '加权移动平均',
        '2025-01-01 00:00:00',
        '2025-04-30 23:59:59',
        '2025-05-01 00:00:00',
        '2025-05-31 23:59:59',
        '2025-04-30 23:59:59',
        31,
        120,
        'INITIAL',
        'UNADJUSTED',
        'SUCCESS',
        'ATTENTION',
        4,
        '2025-04-30 10:15:00',
        '2025-04-30 10:15:12',
        '基于河南 1-4 月整仓平均温度预测风险仓 5 月走势',
        'Phase 11 风险仓演示基线'
    ),
    (
        'TASK-PHASE11-STABLE-001',
        NULL,
        1,
        1,
        'temperature',
        'GRAIN_TEMP_SUMMARY',
        'AVG_TEMP',
        'LINEAR_REGRESSION',
        '线性回归',
        '2025-01-01 00:00:00',
        '2025-04-30 23:59:59',
        '2025-05-01 00:00:00',
        '2025-05-15 23:59:59',
        '2025-04-30 23:59:59',
        15,
        120,
        'INITIAL',
        'UNADJUSTED',
        'SUCCESS',
        'NORMAL',
        2,
        '2025-04-30 09:40:00',
        '2025-04-30 09:40:08',
        '稳定仓 5 月预测保持低风险，用于答辩时对照展示',
        'Phase 11 稳定仓演示基线'
    ),
    (
        'TASK-PHASE11-CONTRAST-001',
        NULL,
        1,
        6,
        'temperature',
        'GRAIN_TEMP_SUMMARY',
        'AVG_TEMP',
        'LINEAR_REGRESSION',
        '线性回归',
        '2025-01-01 00:00:00',
        '2025-04-30 23:59:59',
        '2025-05-01 00:00:00',
        '2025-05-21 23:59:59',
        '2025-04-30 23:59:59',
        21,
        120,
        'INITIAL',
        'UNADJUSTED',
        'SUCCESS',
        'ATTENTION',
        6,
        '2025-04-30 11:05:00',
        '2025-04-30 11:05:10',
        '对比仓 5 月预测保留一定波动，用于展示与风险仓不同的升温节奏',
        'Phase 11 对比仓演示基线'
    );

INSERT INTO prediction_result (
    task_id,
    phase_type,
    step_index,
    result_time,
    actual_value,
    predicted_value,
    error_value,
    error_rate,
    warning_level,
    warning_flag,
    warning_message,
    is_corrected,
    remark
)
SELECT
    task.id,
    seed.phase_type,
    seed.step_index,
    seed.result_time,
    seed.actual_value,
    seed.predicted_value,
    seed.error_value,
    seed.error_rate,
    seed.warning_level,
    seed.warning_flag,
    seed.warning_message,
    seed.is_corrected,
    seed.remark
FROM prediction_task task
JOIN (
    SELECT 'TASK-PHASE11-RISK-001' AS task_no, 'FUTURE' AS phase_type, 1 AS step_index, '2025-05-01 00:00:00' AS result_time, NULL AS actual_value, 26.55 AS predicted_value, NULL AS error_value, NULL AS error_rate, 'ATTENTION' AS warning_level, 1 AS warning_flag, '5 月初风险仓继续缓慢抬升，仍需重点巡检。' AS warning_message, 0 AS is_corrected, 'Phase 11 风险仓未来预测点' AS remark
    UNION ALL
    SELECT 'TASK-PHASE11-RISK-001', 'FUTURE', 8, '2025-05-08 00:00:00', NULL, 26.72, NULL, NULL, 'ATTENTION', 1, '进入 5 月第二周后接近阈值，建议预留通风窗口。', 0, 'Phase 11 风险仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-RISK-001', 'FUTURE', 15, '2025-05-15 00:00:00', NULL, 26.88, NULL, NULL, 'ATTENTION', 1, '月中仍维持高位，是答辩里的重点风险样本。', 0, 'Phase 11 风险仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-RISK-001', 'FUTURE', 22, '2025-05-22 00:00:00', NULL, 27.02, NULL, NULL, 'ATTENTION', 1, '5 月下旬预计逼近高温阈值，需持续关注。', 0, 'Phase 11 风险仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-RISK-001', 'FUTURE', 31, '2025-05-31 00:00:00', NULL, 27.16, NULL, NULL, 'ATTENTION', 1, '月底仍处高位，延续风险仓升温叙事。', 0, 'Phase 11 风险仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-STABLE-001', 'FUTURE', 1, '2025-05-01 00:00:00', NULL, 20.36, NULL, NULL, 'NORMAL', 0, NULL, 0, 'Phase 11 稳定仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-STABLE-001', 'FUTURE', 7, '2025-05-07 00:00:00', NULL, 20.44, NULL, NULL, 'NORMAL', 0, NULL, 0, 'Phase 11 稳定仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-STABLE-001', 'FUTURE', 15, '2025-05-15 00:00:00', NULL, 20.55, NULL, NULL, 'NORMAL', 0, NULL, 0, 'Phase 11 稳定仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-CONTRAST-001', 'FUTURE', 1, '2025-05-01 00:00:00', NULL, 23.22, NULL, NULL, 'NORMAL', 0, NULL, 0, 'Phase 11 对比仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-CONTRAST-001', 'FUTURE', 7, '2025-05-07 00:00:00', NULL, 23.38, NULL, NULL, 'NORMAL', 0, NULL, 0, 'Phase 11 对比仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-CONTRAST-001', 'FUTURE', 14, '2025-05-14 00:00:00', NULL, 23.51, NULL, NULL, 'ATTENTION', 1, '对比仓保持轻微波动，可与风险仓形成对照。', 0, 'Phase 11 对比仓未来预测点'
    UNION ALL
    SELECT 'TASK-PHASE11-CONTRAST-001', 'FUTURE', 21, '2025-05-21 00:00:00', NULL, 23.66, NULL, NULL, 'ATTENTION', 1, '5 月下旬仍有轻微抬升，但整体弱于风险仓。', 0, 'Phase 11 对比仓未来预测点'
) seed ON seed.task_no = task.task_no;
