CREATE DATABASE IF NOT EXISTS grain_env_predict
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE grain_env_predict;

CREATE TABLE IF NOT EXISTS warehouse (
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

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_user (
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
    CONSTRAINT fk_user_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sensor_metric (
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

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码',
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
    CONSTRAINT fk_sensor_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    CONSTRAINT fk_sensor_metric
        FOREIGN KEY (metric_code) REFERENCES sensor_metric (metric_code),
    CONSTRAINT fk_sensor_created_by
        FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境监测数据表，一行表示一个仓库在某时刻的单个指标值';

CREATE TABLE IF NOT EXISTS prediction_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_no VARCHAR(64) NOT NULL COMMENT '预测任务编号',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    metric_code VARCHAR(64) NOT NULL COMMENT '预测指标编码',
    algorithm_code VARCHAR(64) NOT NULL COMMENT '算法编码',
    algorithm_name VARCHAR(64) NOT NULL COMMENT '算法名称',
    future_steps INT NOT NULL COMMENT '向后预测步数',
    sample_size INT NOT NULL DEFAULT 0 COMMENT '实际参与计算的样本数',
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '任务状态：SUCCESS/FAILED',
    risk_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '风险等级：NORMAL/ATTENTION/WARNING',
    requested_by BIGINT DEFAULT NULL COMMENT '发起人',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    summary VARCHAR(255) DEFAULT NULL COMMENT '任务摘要或失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_prediction_task_no (task_no),
    KEY idx_prediction_task_query (warehouse_id, metric_code, requested_at),
    KEY idx_prediction_task_status (status),
    CONSTRAINT fk_prediction_task_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    CONSTRAINT fk_prediction_task_metric
        FOREIGN KEY (metric_code) REFERENCES sensor_metric (metric_code),
    CONSTRAINT fk_prediction_task_user
        FOREIGN KEY (requested_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测任务主表，一次预测请求一条记录';

CREATE TABLE IF NOT EXISTS prediction_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '预测任务ID',
    step_index INT NOT NULL COMMENT '未来步数序号，从1开始',
    predicted_time DATETIME NOT NULL COMMENT '预测时间点',
    actual_value DECIMAL(10, 2) DEFAULT NULL COMMENT '真实值，后续回填时使用',
    predicted_value DECIMAL(10, 2) NOT NULL COMMENT '预测值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_prediction_result_step (task_id, step_index),
    KEY idx_prediction_result_time (predicted_time),
    CONSTRAINT fk_prediction_result_task
        FOREIGN KEY (task_id) REFERENCES prediction_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测结果明细表，一次任务对应多个未来时间点';

INSERT INTO warehouse (
    id,
    warehouse_code,
    warehouse_name,
    location,
    capacity_ton,
    manager_name,
    contact_phone,
    status,
    grain_type,
    remark
)
VALUES
    (1, 'WH-A01', '一号平房仓', '东区 1 号库', 1200.00, '张建国', '13800000001', 'ACTIVE', '小麦', '答辩演示默认仓库'),
    (2, 'WH-A02', '二号平房仓', '东区 2 号库', 980.00, '李春华', '13800000002', 'WARNING', '玉米', '当前存在温度波动'),
    (3, 'WH-B01', '三号立筒仓', '西区 1 号库', 1500.00, '王利民', '13800000003', 'MAINTENANCE', '稻谷', '维护中'),
    (4, 'WH-B02', '四号平房仓', '西区 2 号库', 1100.00, '赵志勇', '13800000004', 'ACTIVE', '大豆', '近期通风良好'),
    (5, 'WH-C01', '五号浅圆仓', '南区 1 号库', 860.00, '孙海燕', '13800000005', 'ACTIVE', '小麦', '适合作为低风险演示仓库'),
    (6, 'WH-C02', '六号立筒仓', '南区 2 号库', 1320.00, '周广林', '13800000006', 'WARNING', '玉米', '夜间湿度偏高，需重点关注')
ON DUPLICATE KEY UPDATE
    warehouse_name = VALUES(warehouse_name),
    location = VALUES(location),
    capacity_ton = VALUES(capacity_ton),
    manager_name = VALUES(manager_name),
    contact_phone = VALUES(contact_phone),
    status = VALUES(status),
    grain_type = VALUES(grain_type),
    remark = VALUES(remark);

INSERT INTO sys_role (id, role_code, role_name, role_desc)
VALUES
    (1, 'ADMIN', '管理员', '管理用户、仓库、全部环境数据和预测记录'),
    (2, 'WAREHOUSE_MANAGER', '仓库管理员', '维护所属仓库环境数据并执行预测'),
    (3, 'VIEWER', '查看者', '只读查看仪表盘、图表和预测结果')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    role_desc = VALUES(role_desc);

INSERT INTO sys_user (
    id,
    username,
    password,
    display_name,
    phone,
    warehouse_id,
    status
)
VALUES
    (1, 'admin', '123456', '系统管理员', '13800001000', NULL, 'ACTIVE'),
    (2, 'manager_a01', '123456', '东一区管理员', '13800001001', 1, 'ACTIVE'),
    (3, 'viewer_demo', '123456', '演示查看者', '13800001002', NULL, 'ACTIVE'),
    (4, 'manager_a02', '123456', '东二区管理员', '13800001003', 2, 'ACTIVE'),
    (5, 'manager_b02', '123456', '西二区管理员', '13800001004', 4, 'ACTIVE'),
    (6, 'manager_c01', '123456', '南一区管理员', '13800001005', 5, 'ACTIVE'),
    (7, 'viewer_ops', '123456', '运维查看者', '13800001006', NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    phone = VALUES(phone),
    warehouse_id = VALUES(warehouse_id),
    status = VALUES(status);

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 2),
    (5, 2),
    (6, 2),
    (7, 3)
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id);

INSERT INTO sensor_metric (
    id,
    metric_code,
    metric_name,
    unit,
    min_threshold,
    max_threshold,
    status
)
VALUES
    (1, 'temperature', '温度', '°C', 5.00, 30.00, 'ACTIVE'),
    (2, 'humidity', '湿度', '%', 30.00, 75.00, 'ACTIVE'),
    (3, 'co2', '二氧化碳浓度', 'ppm', 300.00, 1500.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    metric_name = VALUES(metric_name),
    unit = VALUES(unit),
    min_threshold = VALUES(min_threshold),
    max_threshold = VALUES(max_threshold),
    status = VALUES(status);

INSERT INTO sensor_data (
    id,
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
VALUES
    (1, 1, 'temperature', 24.60, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 2),
    (2, 1, 'humidity', 58.30, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 2),
    (3, 1, 'co2', 620.00, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 2),
    (4, 1, 'temperature', 24.90, '2026-04-07 08:00:00', 'MANUAL', NULL, 'NORMAL', '例行巡检', 2),
    (5, 1, 'humidity', 59.10, '2026-04-07 08:00:00', 'MANUAL', NULL, 'NORMAL', '例行巡检', 2),
    (6, 1, 'co2', 640.00, '2026-04-07 08:00:00', 'MANUAL', NULL, 'NORMAL', '例行巡检', 2),
    (7, 1, 'temperature', 25.40, '2026-04-07 10:00:00', 'MANUAL', NULL, 'NORMAL', '升温观察', 2),
    (8, 1, 'humidity', 57.80, '2026-04-07 10:00:00', 'MANUAL', NULL, 'NORMAL', '升温观察', 2),
    (9, 1, 'co2', 680.00, '2026-04-07 10:00:00', 'MANUAL', NULL, 'NORMAL', '升温观察', 2),
    (10, 2, 'temperature', 26.30, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 4),
    (11, 2, 'humidity', 70.20, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 4),
    (12, 2, 'co2', 720.00, '2026-04-07 06:00:00', 'MANUAL', NULL, 'NORMAL', '早间巡检', 4),
    (13, 2, 'temperature', 27.10, '2026-04-07 08:00:00', 'MANUAL', NULL, 'ATTENTION', '波动偏大', 4),
    (14, 2, 'humidity', 73.50, '2026-04-07 08:00:00', 'MANUAL', NULL, 'ATTENTION', '波动偏大', 4),
    (15, 2, 'co2', 810.00, '2026-04-07 08:00:00', 'MANUAL', NULL, 'NORMAL', '通风前', 4),
    (16, 2, 'temperature', 28.20, '2026-04-07 10:00:00', 'MANUAL', NULL, 'ATTENTION', '高温关注', 4),
    (17, 2, 'humidity', 74.10, '2026-04-07 10:00:00', 'MANUAL', NULL, 'ATTENTION', '高温关注', 4),
    (18, 2, 'co2', 890.00, '2026-04-07 10:00:00', 'MANUAL', NULL, 'NORMAL', '高温关注', 4),
    (19, 4, 'temperature', 23.80, '2026-04-07 07:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (20, 4, 'humidity', 55.40, '2026-04-07 07:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (21, 4, 'co2', 590.00, '2026-04-07 07:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (22, 4, 'temperature', 24.10, '2026-04-07 09:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (23, 4, 'humidity', 56.20, '2026-04-07 09:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (24, 4, 'co2', 610.00, '2026-04-07 09:00:00', 'IMPORT', 'BATCH-DEMO01', 'NORMAL', '批量导入演示', 5),
    (25, 5, 'temperature', 22.90, '2026-04-07 07:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (26, 5, 'humidity', 52.10, '2026-04-07 07:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (27, 5, 'co2', 540.00, '2026-04-07 07:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (28, 5, 'temperature', 23.20, '2026-04-07 09:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (29, 5, 'humidity', 53.00, '2026-04-07 09:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (30, 5, 'co2', 560.00, '2026-04-07 09:30:00', 'MANUAL', NULL, 'NORMAL', '稳定仓库样本', 6),
    (31, 6, 'temperature', 26.80, '2026-04-07 06:30:00', 'MANUAL', NULL, 'ATTENTION', '夜间偏热', 6),
    (32, 6, 'humidity', 75.20, '2026-04-07 06:30:00', 'MANUAL', NULL, 'ATTENTION', '湿度接近阈值', 6),
    (33, 6, 'co2', 930.00, '2026-04-07 06:30:00', 'MANUAL', NULL, 'NORMAL', '夜间偏热', 6),
    (34, 6, 'temperature', 27.50, '2026-04-07 08:30:00', 'MANUAL', NULL, 'ATTENTION', '持续升温', 6),
    (35, 6, 'humidity', 76.40, '2026-04-07 08:30:00', 'MANUAL', NULL, 'ATTENTION', '湿度偏高', 6),
    (36, 6, 'co2', 980.00, '2026-04-07 08:30:00', 'MANUAL', NULL, 'NORMAL', '持续升温', 6)
ON DUPLICATE KEY UPDATE
    warehouse_id = VALUES(warehouse_id),
    metric_code = VALUES(metric_code),
    metric_value = VALUES(metric_value),
    collected_at = VALUES(collected_at),
    source_type = VALUES(source_type),
    source_batch_no = VALUES(source_batch_no),
    quality_flag = VALUES(quality_flag),
    remark = VALUES(remark),
    created_by = VALUES(created_by);

INSERT INTO prediction_task (
    id,
    task_no,
    warehouse_id,
    metric_code,
    algorithm_code,
    algorithm_name,
    future_steps,
    sample_size,
    status,
    risk_level,
    requested_by,
    requested_at,
    completed_at,
    summary
)
VALUES
    (1, 'TASK-DEMO01', 1, 'temperature', 'LINEAR_REGRESSION', '线性回归', 6, 12, 'SUCCESS', 'ATTENTION', 2, '2026-04-07 10:05:00', '2026-04-07 10:05:10', '未来 6 小时温度将缓慢上升'),
    (2, 'TASK-DEMO02', 2, 'temperature', 'WEIGHTED_MOVING_AVERAGE', '加权移动平均', 6, 12, 'SUCCESS', 'WARNING', 4, '2026-04-07 10:10:00', '2026-04-07 10:10:08', '二号平房仓有持续升温风险'),
    (3, 'TASK-DEMO03', 5, 'temperature', 'LINEAR_REGRESSION', '线性回归', 4, 8, 'SUCCESS', 'NORMAL', 6, '2026-04-07 09:40:00', '2026-04-07 09:40:06', '五号浅圆仓走势平稳'),
    (4, 'TASK-DEMO04', 2, 'humidity', 'LINEAR_REGRESSION', '线性回归', 4, 10, 'SUCCESS', 'ATTENTION', 4, '2026-04-07 10:20:00', '2026-04-07 10:20:06', '二号平房仓湿度未来 4 小时仍偏高'),
    (5, 'TASK-DEMO05', 6, 'co2', 'WEIGHTED_MOVING_AVERAGE', '加权移动平均', 4, 10, 'SUCCESS', 'NORMAL', 7, '2026-04-07 10:25:00', '2026-04-07 10:25:07', '六号立筒仓二氧化碳浓度整体平稳')
ON DUPLICATE KEY UPDATE
    warehouse_id = VALUES(warehouse_id),
    metric_code = VALUES(metric_code),
    algorithm_code = VALUES(algorithm_code),
    algorithm_name = VALUES(algorithm_name),
    future_steps = VALUES(future_steps),
    sample_size = VALUES(sample_size),
    status = VALUES(status),
    risk_level = VALUES(risk_level),
    requested_by = VALUES(requested_by),
    requested_at = VALUES(requested_at),
    completed_at = VALUES(completed_at),
    summary = VALUES(summary);

INSERT INTO prediction_result (
    id,
    task_id,
    step_index,
    predicted_time,
    actual_value,
    predicted_value
)
VALUES
    (1, 1, 1, '2026-04-07 11:00:00', NULL, 25.70),
    (2, 1, 2, '2026-04-07 12:00:00', NULL, 25.90),
    (3, 1, 3, '2026-04-07 13:00:00', NULL, 26.10),
    (4, 1, 4, '2026-04-07 14:00:00', NULL, 26.30),
    (5, 1, 5, '2026-04-07 15:00:00', NULL, 26.50),
    (6, 1, 6, '2026-04-07 16:00:00', NULL, 26.70),
    (7, 2, 1, '2026-04-07 11:00:00', NULL, 28.40),
    (8, 2, 2, '2026-04-07 12:00:00', NULL, 28.70),
    (9, 2, 3, '2026-04-07 13:00:00', NULL, 29.00),
    (10, 2, 4, '2026-04-07 14:00:00', NULL, 29.20),
    (11, 2, 5, '2026-04-07 15:00:00', NULL, 29.40),
    (12, 2, 6, '2026-04-07 16:00:00', NULL, 29.60),
    (13, 3, 1, '2026-04-07 10:30:00', NULL, 23.30),
    (14, 3, 2, '2026-04-07 11:30:00', NULL, 23.40),
    (15, 3, 3, '2026-04-07 12:30:00', NULL, 23.50),
    (16, 3, 4, '2026-04-07 13:30:00', NULL, 23.60),
    (17, 4, 1, '2026-04-07 11:00:00', NULL, 74.60),
    (18, 4, 2, '2026-04-07 12:00:00', NULL, 74.90),
    (19, 4, 3, '2026-04-07 13:00:00', NULL, 75.20),
    (20, 4, 4, '2026-04-07 14:00:00', NULL, 75.40),
    (21, 5, 1, '2026-04-07 11:00:00', NULL, 1000.00),
    (22, 5, 2, '2026-04-07 12:00:00', NULL, 1020.00),
    (23, 5, 3, '2026-04-07 13:00:00', NULL, 1040.00),
    (24, 5, 4, '2026-04-07 14:00:00', NULL, 1050.00)
ON DUPLICATE KEY UPDATE
    task_id = VALUES(task_id),
    step_index = VALUES(step_index),
    predicted_time = VALUES(predicted_time),
    actual_value = VALUES(actual_value),
    predicted_value = VALUES(predicted_value);
