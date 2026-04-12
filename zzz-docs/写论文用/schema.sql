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
    (6, 'WH-C02', '六号立筒仓', '南区 2 号库', 1320.00, '周广林', '13800000006', 'WARNING', '玉米', '修正明显仓样本');

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

INSERT INTO sensor_data (id, warehouse_id, metric_code, metric_value, collected_at, source_type, source_batch_no, quality_flag, remark, created_by)
VALUES
    (1, 1, 'humidity', 55.20, '2025-09-30 08:40:00', 'MANUAL', NULL, 'NORMAL', '稳定仓月末样本', 2),
    (2, 1, 'co2', 560.00, '2025-09-30 08:40:00', 'MANUAL', NULL, 'NORMAL', '稳定仓月末样本', 2),
    (3, 2, 'humidity', 73.10, '2025-09-30 08:40:00', 'MANUAL', NULL, 'ATTENTION', '升温风险仓月末样本', 4),
    (4, 2, 'co2', 880.00, '2025-09-30 08:40:00', 'MANUAL', NULL, 'NORMAL', '升温风险仓月末样本', 4),
    (5, 6, 'humidity', 74.60, '2025-09-30 08:40:00', 'MANUAL', NULL, 'ATTENTION', '修正明显仓月末样本', 6),
    (6, 6, 'co2', 930.00, '2025-09-30 08:40:00', 'MANUAL', NULL, 'NORMAL', '修正明显仓月末样本', 6);

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

INSERT INTO grain_temp_record (id, warehouse_id, point_id, collected_at, temperature_value, source_type, batch_no, quality_flag, remark, created_by)
VALUES
    (1, 1, 1, '2025-08-31 08:40:00', 23.20, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (2, 1, 2, '2025-08-31 08:40:00', 23.40, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (3, 1, 3, '2025-08-31 08:40:00', 23.60, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (4, 1, 4, '2025-08-31 08:40:00', 23.50, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (5, 1, 5, '2025-08-31 08:40:00', 23.30, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (6, 1, 6, '2025-08-31 08:40:00', 23.40, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (7, 1, 7, '2025-08-31 08:40:00', 23.70, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (8, 1, 8, '2025-08-31 08:40:00', 23.60, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (9, 1, 9, '2025-08-31 08:40:00', 23.50, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (10, 1, 10, '2025-08-31 08:40:00', 23.70, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (11, 1, 11, '2025-08-31 08:40:00', 23.80, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (12, 1, 12, '2025-08-31 08:40:00', 23.90, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (13, 1, 13, '2025-08-31 08:40:00', 23.60, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (14, 1, 14, '2025-08-31 08:40:00', 23.80, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (15, 1, 15, '2025-08-31 08:40:00', 24.00, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (16, 1, 16, '2025-08-31 08:40:00', 24.10, 'IMPORT', 'BATCH-GRAIN-001', 'NORMAL', '稳定仓导入样本', 2),
    (17, 2, 17, '2025-09-30 08:40:00', 26.60, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (18, 2, 18, '2025-09-30 08:40:00', 26.90, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (19, 2, 19, '2025-09-30 08:40:00', 27.10, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (20, 2, 20, '2025-09-30 08:40:00', 27.30, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (21, 2, 21, '2025-09-30 08:40:00', 27.00, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (22, 2, 22, '2025-09-30 08:40:00', 27.20, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (23, 2, 23, '2025-09-30 08:40:00', 27.40, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (24, 2, 24, '2025-09-30 08:40:00', 27.60, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (25, 2, 25, '2025-09-30 08:40:00', 27.30, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (26, 2, 26, '2025-09-30 08:40:00', 27.50, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (27, 2, 27, '2025-09-30 08:40:00', 27.70, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (28, 2, 28, '2025-09-30 08:40:00', 27.90, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (29, 2, 29, '2025-09-30 08:40:00', 27.60, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (30, 2, 30, '2025-09-30 08:40:00', 27.80, 'IMPORT', 'BATCH-GRAIN-002', 'NORMAL', '风险仓导入样本', 4),
    (31, 2, 31, '2025-09-30 08:40:00', 28.20, 'IMPORT', 'BATCH-GRAIN-002', 'ATTENTION', '风险仓导入样本', 4),
    (32, 2, 32, '2025-09-30 08:40:00', 28.60, 'IMPORT', 'BATCH-GRAIN-002', 'ATTENTION', '风险仓导入样本', 4),
    (33, 6, 33, '2025-09-30 08:40:00', 25.50, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (34, 6, 34, '2025-09-30 08:40:00', 25.90, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (35, 6, 35, '2025-09-30 08:40:00', 26.10, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (36, 6, 36, '2025-09-30 08:40:00', 26.30, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (37, 6, 37, '2025-09-30 08:40:00', 25.80, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (38, 6, 38, '2025-09-30 08:40:00', 26.00, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (39, 6, 39, '2025-09-30 08:40:00', 26.20, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (40, 6, 40, '2025-09-30 08:40:00', 26.40, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (41, 6, 41, '2025-09-30 08:40:00', 26.10, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (42, 6, 42, '2025-09-30 08:40:00', 26.30, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (43, 6, 43, '2025-09-30 08:40:00', 26.50, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (44, 6, 44, '2025-09-30 08:40:00', 26.70, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (45, 6, 45, '2025-09-30 08:40:00', 26.20, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (46, 6, 46, '2025-09-30 08:40:00', 26.40, 'IMPORT', 'BATCH-GRAIN-003', 'NORMAL', '修正仓导入样本', 6),
    (47, 6, 47, '2025-09-30 08:40:00', 26.80, 'IMPORT', 'BATCH-GRAIN-003', 'ATTENTION', '修正仓导入样本', 6),
    (48, 6, 48, '2025-09-30 08:40:00', 27.20, 'IMPORT', 'BATCH-GRAIN-003', 'ATTENTION', '修正仓导入样本', 6);

INSERT INTO grain_temp_summary (id, warehouse_id, collected_at, avg_temp, max_temp, min_temp, layer_1_avg, layer_2_avg, layer_3_avg, layer_4_avg, warning_level, warning_flag, warning_message, analysis_result, analysis_remark)
VALUES
    (1, 1, '2025-01-31 08:40:00', 22.80, 23.60, 22.10, 22.60, 22.70, 22.90, 23.00, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (2, 1, '2025-02-28 08:40:00', 22.90, 23.70, 22.20, 22.70, 22.80, 23.00, 23.10, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (3, 1, '2025-03-31 08:40:00', 23.00, 23.80, 22.30, 22.80, 22.90, 23.10, 23.20, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (4, 1, '2025-04-30 08:40:00', 23.10, 23.90, 22.40, 22.90, 23.00, 23.20, 23.30, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (5, 1, '2025-05-31 08:40:00', 23.30, 24.10, 22.60, 23.10, 23.20, 23.40, 23.50, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (6, 1, '2025-06-30 08:40:00', 23.50, 24.30, 22.70, 23.30, 23.40, 23.60, 23.70, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (7, 1, '2025-07-31 08:40:00', 23.60, 24.40, 22.90, 23.40, 23.50, 23.70, 23.80, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (8, 1, '2025-08-31 08:40:00', 23.80, 24.60, 23.20, 23.55, 23.65, 23.85, 24.05, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (9, 1, '2025-09-30 08:40:00', 23.90, 24.70, 23.30, 23.70, 23.80, 23.95, 24.15, 'NORMAL', 0, NULL, '粮温正常', '稳定仓月度汇总'),
    (10, 2, '2025-01-31 08:40:00', 22.40, 23.10, 21.80, 22.10, 22.20, 22.50, 22.80, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (11, 2, '2025-02-28 08:40:00', 22.80, 23.50, 22.10, 22.50, 22.60, 22.90, 23.20, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (12, 2, '2025-03-31 08:40:00', 23.10, 23.90, 22.40, 22.80, 22.90, 23.20, 23.50, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (13, 2, '2025-04-30 08:40:00', 23.50, 24.30, 22.80, 23.20, 23.30, 23.60, 23.90, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (14, 2, '2025-05-31 08:40:00', 24.00, 24.80, 23.20, 23.70, 23.80, 24.10, 24.40, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (15, 2, '2025-06-30 08:40:00', 24.60, 25.50, 23.70, 24.20, 24.40, 24.70, 25.10, 'NORMAL', 0, NULL, '粮温正常', '风险仓月度汇总'),
    (16, 2, '2025-07-31 08:40:00', 25.50, 26.40, 24.60, 25.10, 25.30, 25.60, 26.00, 'ATTENTION', 1, '仓温持续抬升，需关注高温趋势', '粮温关注', '风险仓月度汇总'),
    (17, 2, '2025-08-31 08:40:00', 26.10, 27.00, 25.10, 25.70, 25.90, 26.20, 26.60, 'ATTENTION', 1, '接近高温阈值，建议提前通风', '粮温关注', '风险仓月度汇总'),
    (18, 2, '2025-09-30 08:40:00', 27.40, 28.60, 26.60, 26.98, 27.30, 27.60, 27.72, 'WARNING', 1, '二层与四层温度偏高，存在高温风险', '高温预警', '风险仓月度汇总'),
    (19, 6, '2025-01-31 08:40:00', 23.40, 24.10, 22.70, 23.10, 23.20, 23.50, 23.80, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (20, 6, '2025-02-28 08:40:00', 23.80, 24.60, 23.00, 23.40, 23.60, 23.90, 24.20, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (21, 6, '2025-03-31 08:40:00', 24.10, 24.90, 23.30, 23.80, 23.90, 24.20, 24.50, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (22, 6, '2025-04-30 08:40:00', 24.50, 25.30, 23.70, 24.10, 24.30, 24.60, 24.90, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (23, 6, '2025-05-31 08:40:00', 24.90, 25.70, 24.10, 24.50, 24.70, 25.00, 25.30, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (24, 6, '2025-06-30 08:40:00', 25.40, 26.10, 24.50, 25.00, 25.20, 25.50, 25.90, 'NORMAL', 0, NULL, '粮温正常', '修正仓月度汇总'),
    (25, 6, '2025-07-31 08:40:00', 25.90, 26.70, 25.10, 25.50, 25.70, 26.00, 26.40, 'ATTENTION', 1, '温度波动偏大，后续需验证预测准确性', '粮温关注', '修正仓月度汇总'),
    (26, 6, '2025-08-31 08:40:00', 26.10, 27.00, 25.20, 25.70, 25.90, 26.20, 26.60, 'ATTENTION', 1, '接近阈值，建议加强监测', '粮温关注', '修正仓月度汇总'),
    (27, 6, '2025-09-30 08:40:00', 26.40, 27.20, 25.50, 25.95, 26.10, 26.40, 27.00, 'ATTENTION', 1, '四层温度偏高，需验证修正预测', '粮温关注', '修正仓月度汇总');

INSERT INTO prediction_task (
    id, task_no, parent_task_id, task_round, warehouse_id, metric_code, data_source_type, target_type,
    algorithm_code, algorithm_name, train_start_time, train_end_time, forecast_start_time, forecast_end_time,
    based_on_actual_end_time, forecast_days, sample_size, trigger_type, adjust_status, status, risk_level,
    requested_by, requested_at, completed_at, summary, remark
)
VALUES
    (1, 'TASK-ROLLING-001', NULL, 1, 2, 'temperature', 'GRAIN_TEMP_SUMMARY', 'AVG_TEMP',
     'LINEAR_REGRESSION', '线性回归', '2025-01-01 00:00:00', '2025-08-31 23:59:59', '2025-09-01 00:00:00', '2025-10-31 23:59:59',
     '2025-08-31 23:59:59', 61, 8, 'INITIAL', 'UNADJUSTED', 'SUCCESS', 'WARNING',
     4, '2025-08-31 10:05:00', '2025-08-31 10:05:08', '基于前8个月整仓平均温度预测9月与10月走势', '首次预测'),
    (2, 'TASK-ROLLING-002', 1, 2, 2, 'temperature', 'GRAIN_TEMP_SUMMARY', 'AVG_TEMP',
     'WEIGHTED_MOVING_AVERAGE', '加权移动平均', '2025-01-01 00:00:00', '2025-09-30 23:59:59', '2025-10-01 00:00:00', '2025-11-30 23:59:59',
     '2025-09-30 23:59:59', 61, 9, 'CORRECTION', 'ADJUSTED', 'SUCCESS', 'WARNING',
     4, '2025-10-01 09:20:00', '2025-10-01 09:20:10', '9月真实数据回填后修正10月并继续预测11月', '修正预测'),
    (3, 'TASK-ROLLING-003', NULL, 1, 1, 'temperature', 'GRAIN_TEMP_SUMMARY', 'AVG_TEMP',
     'LINEAR_REGRESSION', '线性回归', '2025-01-01 00:00:00', '2025-08-31 23:59:59', '2025-09-01 00:00:00', '2025-10-31 23:59:59',
     '2025-08-31 23:59:59', 61, 8, 'INITIAL', 'UNADJUSTED', 'SUCCESS', 'NORMAL',
     2, '2025-08-31 09:40:00', '2025-08-31 09:40:06', '稳定仓预测样本，用于展示低风险低误差场景', '稳定仓'),
    (4, 'TASK-ROLLING-004', NULL, 1, 6, 'temperature', 'GRAIN_TEMP_SUMMARY', 'AVG_TEMP',
     'LINEAR_REGRESSION', '线性回归', '2025-01-01 00:00:00', '2025-08-31 23:59:59', '2025-09-01 00:00:00', '2025-10-31 23:59:59',
     '2025-08-31 23:59:59', 61, 8, 'INITIAL', 'UNADJUSTED', 'SUCCESS', 'ATTENTION',
     6, '2025-08-31 11:15:00', '2025-08-31 11:15:07', '修正明显仓首次预测，用于展示误差验证价值', '修正仓');

INSERT INTO prediction_result (
    id, task_id, phase_type, step_index, result_time, actual_value, predicted_value, error_value, error_rate,
    warning_level, warning_flag, warning_message, is_corrected, remark
)
VALUES
    (1, 1, 'VERIFY', 1, '2025-09-01 00:00:00', 26.80, 26.30, 0.50, 1.90, 'ATTENTION', 1, '接近高温阈值，需关注升温趋势', 0, '首轮验证点'),
    (2, 1, 'VERIFY', 15, '2025-09-15 00:00:00', 27.10, 26.60, 0.50, 1.85, 'ATTENTION', 1, '月中温度持续偏高', 0, '首轮验证点'),
    (3, 1, 'VERIFY', 30, '2025-09-30 00:00:00', 27.40, 27.90, -0.50, 1.82, 'ATTENTION', 1, '预测偏高但风险仍存在', 0, '首轮验证点'),
    (4, 1, 'FUTURE', 31, '2025-10-01 00:00:00', NULL, 28.10, NULL, NULL, 'WARNING', 1, '预计10月上旬进入高温预警区间', 0, '首轮未来预测'),
    (5, 1, 'FUTURE', 45, '2025-10-15 00:00:00', NULL, 28.40, NULL, NULL, 'WARNING', 1, '预计10月中旬持续高温', 0, '首轮未来预测'),
    (6, 1, 'FUTURE', 61, '2025-10-31 00:00:00', NULL, 28.70, NULL, NULL, 'WARNING', 1, '预计10月末高温风险进一步上升', 0, '首轮未来预测'),
    (7, 2, 'FUTURE', 1, '2025-10-01 00:00:00', NULL, 28.40, NULL, NULL, 'WARNING', 1, '修正后10月初仍有高温风险', 1, '修正后预测'),
    (8, 2, 'FUTURE', 15, '2025-10-15 00:00:00', NULL, 28.80, NULL, NULL, 'WARNING', 1, '修正后10月中旬高温风险更明显', 1, '修正后预测'),
    (9, 2, 'FUTURE', 31, '2025-10-31 00:00:00', NULL, 29.10, NULL, NULL, 'WARNING', 1, '修正后10月末预计超过阈值', 1, '修正后预测'),
    (10, 2, 'FUTURE', 32, '2025-11-01 00:00:00', NULL, 29.20, NULL, NULL, 'WARNING', 1, '11月初仍有延续性高温风险', 1, '修正后预测'),
    (11, 2, 'FUTURE', 46, '2025-11-15 00:00:00', NULL, 29.40, NULL, NULL, 'WARNING', 1, '11月中旬预计仍需重点关注', 1, '修正后预测'),
    (12, 2, 'FUTURE', 61, '2025-11-30 00:00:00', NULL, 29.60, NULL, NULL, 'WARNING', 1, '11月末高温风险未完全解除', 1, '修正后预测'),
    (13, 3, 'VERIFY', 1, '2025-09-01 00:00:00', 23.70, 23.60, 0.10, 0.42, 'NORMAL', 0, NULL, 0, '稳定仓验证点'),
    (14, 3, 'VERIFY', 30, '2025-09-30 00:00:00', 23.90, 23.80, 0.10, 0.42, 'NORMAL', 0, NULL, 0, '稳定仓验证点'),
    (15, 3, 'FUTURE', 31, '2025-10-01 00:00:00', NULL, 23.90, NULL, NULL, 'NORMAL', 0, NULL, 0, '稳定仓未来预测'),
    (16, 3, 'FUTURE', 61, '2025-10-31 00:00:00', NULL, 24.00, NULL, NULL, 'NORMAL', 0, NULL, 0, '稳定仓未来预测'),
    (17, 4, 'VERIFY', 1, '2025-09-01 00:00:00', 26.40, 25.90, 0.50, 1.89, 'ATTENTION', 1, '预测偏低，需要后续修正', 0, '修正仓验证点'),
    (18, 4, 'VERIFY', 30, '2025-09-30 00:00:00', 26.70, 26.10, 0.60, 2.25, 'ATTENTION', 1, '月底误差扩大，适合展示修正价值', 0, '修正仓验证点'),
    (19, 4, 'FUTURE', 31, '2025-10-01 00:00:00', NULL, 26.50, NULL, NULL, 'ATTENTION', 1, '预计10月初继续升温', 0, '修正仓未来预测'),
    (20, 4, 'FUTURE', 61, '2025-10-31 00:00:00', NULL, 27.10, NULL, NULL, 'ATTENTION', 1, '预计10月末接近高温阈值', 0, '修正仓未来预测');
