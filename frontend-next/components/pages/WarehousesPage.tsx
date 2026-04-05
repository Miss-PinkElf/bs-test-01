"use client";

import { Button, Card, Descriptions, Form, Input, Row, Col, Select, Space, Table, Tag } from "antd";
import { warehouseColumnsData, warehouseRecords } from "@/mock/grain-data";

const warehouseColumns = [
  { title: "仓库编码", dataIndex: "code", key: "code" },
  { title: "仓库名称", dataIndex: "name", key: "name" },
  { title: "负责人", dataIndex: "managerName", key: "managerName" },
  { title: "容量(吨)", dataIndex: "capacityTon", key: "capacityTon" },
  { title: "位置", dataIndex: "location", key: "location" },
  {
    title: "状态",
    dataIndex: "statusLabel",
    key: "statusLabel",
    render: (statusLabel: string, record: (typeof warehouseRecords)[number]) => (
      <Tag color={record.statusColor}>{statusLabel}</Tag>
    )
  }
];

export function WarehousesPage() {
  const firstWarehouse = warehouseRecords[0];

  return (
    <div className="section-stack">
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card className="panel-card" title="仓库列表" extra={<Button type="primary">新增仓库</Button>}>
            <Space direction="vertical" size={16} style={{ width: "100%" }}>
              <Form layout="inline">
                <Form.Item label="仓库名称">
                  <Input placeholder="输入仓库名称" style={{ width: 180 }} />
                </Form.Item>
                <Form.Item label="状态">
                  <Select
                    defaultValue="all"
                    options={[
                      { value: "all", label: "全部" },
                      { value: "running", label: "运行中" },
                      { value: "maintenance", label: "维护中" }
                    ]}
                    style={{ width: 140 }}
                  />
                </Form.Item>
                <Button>查询</Button>
              </Form>
              <Table rowKey="id" columns={warehouseColumns} dataSource={warehouseRecords} pagination={false} />
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card className="panel-card" title="当前选中仓库">
            <Descriptions column={1} size="small">
              {warehouseColumnsData(firstWarehouse).map((item) => (
                <Descriptions.Item key={item.label} label={item.label}>
                  {item.value}
                </Descriptions.Item>
              ))}
            </Descriptions>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
