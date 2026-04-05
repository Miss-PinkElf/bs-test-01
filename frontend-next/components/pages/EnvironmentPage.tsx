"use client";

import { Card, DatePicker, Form, Input, Row, Col, Select, Space, Table, Tag, Typography } from "antd";
import { environmentRows, metricOverview } from "@/mock/grain-data";

const { RangePicker } = DatePicker;
const { Text } = Typography;

const dataColumns = [
  { title: "仓库", dataIndex: "warehouseName", key: "warehouseName" },
  { title: "指标", dataIndex: "metricTypeLabel", key: "metricTypeLabel" },
  { title: "采集值", dataIndex: "metricValueLabel", key: "metricValueLabel" },
  { title: "采集时间", dataIndex: "collectedAt", key: "collectedAt" },
  {
    title: "状态",
    dataIndex: "statusLabel",
    key: "statusLabel",
    render: (statusLabel: string, record: (typeof environmentRows)[number]) => (
      <Tag color={record.statusColor}>{statusLabel}</Tag>
    )
  }
];

export function EnvironmentPage() {
  return (
    <div className="section-stack">
      <Card className="panel-card" title="筛选条件">
        <Form layout="inline">
          <Form.Item label="仓库">
            <Select
              defaultValue="all"
              style={{ width: 160 }}
              options={[
                { value: "all", label: "全部仓库" },
                { value: "WH-A01", label: "一号粮仓" },
                { value: "WH-B02", label: "二号粮仓" },
                { value: "WH-C03", label: "三号粮仓" }
              ]}
            />
          </Form.Item>
          <Form.Item label="指标">
            <Select
              defaultValue="temperature"
              style={{ width: 160 }}
              options={[
                { value: "temperature", label: "温度" },
                { value: "humidity", label: "湿度" },
                { value: "oxygen", label: "氧气浓度" }
              ]}
            />
          </Form.Item>
          <Form.Item label="时间范围">
            <RangePicker />
          </Form.Item>
          <Form.Item>
            <Input.Search placeholder="输入关键字" style={{ width: 220 }} enterButton="查询" />
          </Form.Item>
        </Form>
      </Card>

      <Row gutter={[16, 16]}>
        {metricOverview.map((item) => (
          <Col xs={24} md={8} key={item.label}>
            <Card className="panel-card">
              <Space direction="vertical" size={8}>
                <Text strong>{item.label}</Text>
                <div style={{ fontSize: 30, fontWeight: 700 }}>{item.value}</div>
                <div className="metric-pill-group">
                  {item.tags.map((tag) => (
                    <span className="metric-pill" key={tag}>
                      {tag}
                    </span>
                  ))}
                </div>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      <Card className="panel-card" title="环境数据记录">
        <Table rowKey="id" columns={dataColumns} dataSource={environmentRows} pagination={{ pageSize: 6 }} />
      </Card>
    </div>
  );
}
