CREATE DATABASE IF NOT EXISTS grain_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE grain_platform;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(64) NOT NULL UNIQUE,
    warehouse_name VARCHAR(128) NOT NULL,
    location VARCHAR(255),
    capacity_ton INT,
    manager_name VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sensor_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_code VARCHAR(64) NOT NULL UNIQUE,
    metric_name VARCHAR(64) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    metric_value DECIMAL(10, 2) NOT NULL,
    collected_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sensor_query (warehouse_id, metric_code, collected_at)
);

CREATE TABLE IF NOT EXISTS prediction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    algorithm_name VARCHAR(64) NOT NULL,
    predicted_time DATETIME NOT NULL,
    predicted_value DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prediction_query (warehouse_id, metric_code, predicted_time)
);

INSERT INTO sys_role (role_code, role_name)
VALUES ('ADMIN', '管理员'),
       ('WAREHOUSE_MANAGER', '仓库管理员'),
       ('VIEWER', '查看者')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO sensor_metric (metric_code, metric_name, unit)
VALUES ('temperature', '温度', '°C'),
       ('humidity', '湿度', '%')
ON DUPLICATE KEY UPDATE metric_name = VALUES(metric_name), unit = VALUES(unit);
