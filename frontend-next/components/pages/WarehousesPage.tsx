"use client";

import { Button, Card, Col, Descriptions, Form, Input, Row, Select, Space, Table, Tag } from "antd";
import {
  warehouseCapacityRanking,
  warehouseColumnsData,
  warehouseInspectionItems,
  warehouseRecords,
  warehouseSummaryCards
} from "@/mock/grain-data";
import { RankingBarChart } from "@/components/charts/RankingBarChart";

const warehouseColumns = [
  { title: "仓库编码", dataIndex: "code", key: "code" },
  { title: "仓库名称", dataIndex: "name", key: "name" },
  { title: "负责人", dataIndex: "managerName", key: "managerName" },
  { title: "容量(吨)", dataIndex: "capacityTon", key: "capacityTon" },
  { title: "装载率", dataIndex: "utilizationRate", key: "utilizationRate", render: (value: number) => `${value}%` },
  { title: "最近温度", dataIndex: "latestTemperature", key: "latestTemperature" },
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
  const selectedWarehouse = warehouseRecords[0];

  return (
    <div className="section-stack">
      <div className="summary-grid">
        {warehouseSummaryCards.map((item) => (
          <Card className="panel-card" key={item.key}>
            <div className="summary-label">{item.label}</div>
            <div className="summary-value">{item.value}</div>
            <div className="mini-text">{item.note}</div>
          </Card>
        ))}
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card className="panel-card" title="仓库档案列表" extra={<Button type="primary">新增仓库</Button>}>
            <Space direction="vertical" size={16} style={{ width: "100%" }}>
              <Form layout="inline">
                <Form.Item label="仓库名称">
                  <Input placeholder="输入仓库名称" style={{ width: 180 }} />
                </Form.Item>
                <Form.Item label="负责人">
                  <Input placeholder="输入负责人" style={{ width: 180 }} />
                </Form.Item>
                <Form.Item label="状态">
                  <Select
                    defaultValue="all"
                    options={[
                      { value: "all", label: "全部" },
                      { value: "running", label: "运行中" },
                      { value: "attention", label: "关注" },
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
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Card className="panel-card" title="当前选中仓库">
              <Descriptions column={1} size="small">
                {warehouseColumnsData(selectedWarehouse).map((item) => (
                  <Descriptions.Item key={item.label} label={item.label}>
                    {item.value}
                  </Descriptions.Item>
                ))}
              </Descriptions>
            </Card>

            <Card className="panel-card" title="运维待办">
              <div className="compact-list">
                {warehouseInspectionItems.map((item) => (
                  <div className="compact-list-item" key={item}>
                    {item}
                  </div>
                ))}
              </div>
            </Card>
          </Space>
        </Col>
      </Row>

      <Card className="panel-card" title="装载率与运行态势">
        <RankingBarChart items={warehouseCapacityRanking} />
      </Card>
    </div>
  );
}
