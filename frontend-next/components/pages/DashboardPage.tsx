"use client";

import { Alert, Card, Col, List, Progress, Row, Space, Statistic, Table, Tag, Typography } from "antd";
import { dashboardCards, environmentRows, forecastSummary, warehouseAlerts } from "@/mock/grain-data";

const { Paragraph, Text } = Typography;

const metricColumns = [
  { title: "仓库", dataIndex: "warehouseName", key: "warehouseName" },
  { title: "指标", dataIndex: "metricType", key: "metricType" },
  { title: "当前值", dataIndex: "metricValue", key: "metricValue" },
  { title: "采集时间", dataIndex: "collectedAt", key: "collectedAt" }
];

export function DashboardPage() {
  return (
    <div className="section-stack">
      <div className="stat-grid">
        {dashboardCards.map((card) => (
          <Card key={card.key} className="panel-card">
            <Statistic title={card.label} value={card.value} suffix={card.suffix} />
            <div className="mini-text" style={{ marginTop: 10 }}>
              {card.note}
            </div>
          </Card>
        ))}
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card className="panel-card" title="近期预警">
            <List
              dataSource={warehouseAlerts}
              renderItem={(item) => (
                <List.Item>
                  <Space direction="vertical" size={4}>
                    <Space>
                      <Text strong>{item.title}</Text>
                      <Tag color={item.level === "高" ? "red" : "gold"}>{item.level}级</Tag>
                    </Space>
                    <Text type="secondary">{item.description}</Text>
                  </Space>
                </List.Item>
              )}
            />
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card className="panel-card" title="预测摘要">
            <div className="forecast-list">
              {forecastSummary.map((item) => (
                <div className="forecast-item" key={item.time}>
                  <div>
                    <div>{item.time}</div>
                    <div className="mini-text">预计温度</div>
                  </div>
                  <Tag color={item.statusColor}>{item.temperature}</Tag>
                </div>
              ))}
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card className="panel-card" title="最近采样记录">
            <Table
              rowKey="id"
              columns={metricColumns}
              dataSource={environmentRows.slice(0, 5)}
              pagination={false}
              size="middle"
            />
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card className="panel-card" title="系统说明">
            <Space direction="vertical" size={16} style={{ width: "100%" }}>
              <Alert message="当前页面全部使用 mock 数据渲染" type="info" showIcon />
              <Paragraph style={{ margin: 0 }}>
                这版页面主要用于先看毕业设计最终展示效果，后面再决定是否接 MySQL、Spring Boot 和真实权限。
              </Paragraph>
              <div>
                <Text strong>完成度预览</Text>
                <Progress percent={78} strokeColor="#1677ff" />
              </div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
