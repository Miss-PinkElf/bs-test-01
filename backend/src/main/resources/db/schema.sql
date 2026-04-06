CREATE DATABASE IF NOT EXISTS grain_platform
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE grain_platform;

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
    (3, 'WH-B01', '三号立筒仓', '西区 1 号库', 1500.00, '王利民', '13800000003', 'MAINTENANCE', '稻谷', '维护中')
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
    (3, 'viewer_demo', '123456', '演示查看者', '13800001002', NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    phone = VALUES(phone),
    warehouse_id = VALUES(warehouse_id),
    status = VALUES(status);

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3)
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
