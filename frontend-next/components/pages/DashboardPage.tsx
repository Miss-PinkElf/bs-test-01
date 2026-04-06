"use client";

import { Alert, Card, Col, List, Row, Space, Statistic, Table, Tag, Timeline, Typography } from "antd";
import {
  dashboardCards,
  dashboardHighlights,
  dashboardTrendSeries,
  environmentRows,
  forecastSummary,
  processStages,
  warehouseAlerts,
  warehouseHealthRanking
} from "@/mock/grain-data";
import { MultiLineTrendChart } from "@/components/charts/MultiLineTrendChart";
import { RankingBarChart } from "@/components/charts/RankingBarChart";

const { Paragraph, Text } = Typography;

const recentColumns = [
  { title: "仓库", dataIndex: "warehouseName", key: "warehouseName" },
  { title: "指标", dataIndex: "metricTypeLabel", key: "metricTypeLabel" },
  { title: "采样值", dataIndex: "metricValueLabel", key: "metricValueLabel" },
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

export function DashboardPage() {
  return (
    <div className="section-stack">
      <Card className="panel-card hero-card" variant="borderless">
        <div className="hero-grid">
          <div>
            <div className="section-eyebrow">毕业设计成品预期</div>
            <h2 className="hero-title">一个可登录、可管理、可查询、可展示、可预测、可归档的粮仓环境数据平台</h2>
            <Paragraph className="hero-description">
              这版静态原型不追求真数据，而是把最终答辩会展示出来的页面结构、主流程和图表感先做清楚。
            </Paragraph>
            <div className="flow-chip-row">
              {processStages.map((item) => (
                <span className={`flow-chip flow-chip-${item.status}`} key={item.title}>
                  {item.title}
                </span>
              ))}
            </div>
          </div>

          <div className="hero-highlight-list">
            {dashboardHighlights.map((item) => (
              <div className="hero-highlight-item" key={item}>
                {item}
              </div>
            ))}
          </div>
        </div>
      </Card>

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
        <Col xs={24} xl={15}>
          <Card className="panel-card" title="今日采样与预警走势">
            <MultiLineTrendChart series={dashboardTrendSeries} />
          </Card>
        </Col>

        <Col xs={24} xl={9}>
          <Card className="panel-card" title="仓库运行健康度">
            <RankingBarChart items={warehouseHealthRanking} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
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

        <Col xs={24} xl={14}>
          <Card className="panel-card" title="最近采样记录">
            <Table
              rowKey="id"
              columns={recentColumns}
              dataSource={environmentRows}
              pagination={{ pageSize: 5 }}
              size="middle"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
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

        <Col xs={24} xl={8}>
          <Card className="panel-card" title="核心演示脚本">
            <Timeline
              items={processStages.map((item) => ({
                color: item.status === "done" ? "green" : item.status === "active" ? "blue" : "gray",
                children: (
                  <div>
                    <div className="timeline-title">{item.title}</div>
                    <div className="mini-text">{item.description}</div>
                  </div>
                )
              }))}
            />
          </Card>
        </Col>

        <Col xs={24} xl={6}>
          <Card className="panel-card" title="原型说明">
            <Space direction="vertical" size={16} style={{ width: "100%" }}>
              <Alert message="静态原型用途" type="info" showIcon />
              <Paragraph style={{ margin: 0 }}>
                这里主要回答“最终成品大概是什么样”。正式开发时，数据来源会替换成 Spring Boot + MySQL 接口。
              </Paragraph>
              <Paragraph style={{ margin: 0 }}>
                大屏、图表、预测曲线都只做展示，不额外引入超出 PRD 的业务范围。
              </Paragraph>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
