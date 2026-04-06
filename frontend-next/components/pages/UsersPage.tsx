"use client";

import { Button, Card, Col, Row, Space, Table, Tag, Typography } from "antd";
import { permissionMatrix, roleProfiles, userRecords, userSummaryCards } from "@/mock/grain-data";

const { Paragraph, Text } = Typography;

const userColumns = [
  { title: "用户名", dataIndex: "username", key: "username" },
  { title: "姓名", dataIndex: "displayName", key: "displayName" },
  { title: "角色", dataIndex: "roleLabel", key: "roleLabel" },
  { title: "所属范围", dataIndex: "warehouseName", key: "warehouseName" },
  {
    title: "状态",
    dataIndex: "statusLabel",
    key: "statusLabel",
    render: (statusLabel: string, record: (typeof userRecords)[number]) => (
      <Tag color={record.statusColor}>{statusLabel}</Tag>
    )
  },
  { title: "最近登录", dataIndex: "lastLoginAt", key: "lastLoginAt" }
];

const permissionColumns = [
  { title: "模块", dataIndex: "module", key: "module" },
  { title: "管理员", dataIndex: "admin", key: "admin" },
  { title: "仓库管理员", dataIndex: "warehouseManager", key: "warehouseManager" },
  { title: "查看者", dataIndex: "viewer", key: "viewer" }
];

export function UsersPage() {
  return (
    <div className="section-stack">
      <div className="summary-grid">
        {userSummaryCards.map((item) => (
          <Card className="panel-card" key={item.key}>
            <div className="summary-label">{item.label}</div>
            <div className="summary-value">{item.value}</div>
            <div className="mini-text">{item.note}</div>
          </Card>
        ))}
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card className="panel-card" title="用户列表" extra={<Button type="primary">新增用户</Button>}>
            <Table rowKey="id" columns={userColumns} dataSource={userRecords} pagination={false} />
          </Card>
        </Col>

        <Col xs={24} xl={9}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Card className="panel-card" title="角色说明">
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                {roleProfiles.map((role) => (
                  <div className="role-card" key={role.roleCode}>
                    <div className="role-card-title">
                      <Text strong>{role.roleLabel}</Text>
                      <Tag color="blue">{role.roleCode}</Tag>
                    </div>
                    <Paragraph style={{ marginBottom: 10 }}>{role.description}</Paragraph>
                    <div className="metric-pill-group">
                      {role.permissions.map((permission) => (
                        <span className="metric-pill" key={permission}>
                          {permission}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </Space>
            </Card>
          </Space>
        </Col>
      </Row>

      <Card className="panel-card" title="权限矩阵">
        <Table rowKey="module" columns={permissionColumns} dataSource={permissionMatrix} pagination={false} />
      </Card>
    </div>
  );
}
