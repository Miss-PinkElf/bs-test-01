"use client";

import { Button, Card, Col, DatePicker, Form, Input, Row, Select, Space, Table, Tag, Typography } from "antd";
import {
  dataImportSteps,
  environmentMetricRanking,
  environmentOverviewCards,
  environmentRows,
  environmentTrendSeries
} from "@/mock/grain-data";
import { MultiLineTrendChart } from "@/components/charts/MultiLineTrendChart";
import { RankingBarChart } from "@/components/charts/RankingBarChart";

const { RangePicker } = DatePicker;
const { Text } = Typography;

const dataColumns = [
  { title: "仓库", dataIndex: "warehouseName", key: "warehouseName" },
  { title: "指标", dataIndex: "metricTypeLabel", key: "metricTypeLabel" },
  { title: "采集值", dataIndex: "metricValueLabel", key: "metricValueLabel" },
  { title: "采集时间", dataIndex: "collectedAt", key: "collectedAt" },
  { title: "来源", dataIndex: "sourceLabel", key: "sourceLabel" },
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
      <Card className="panel-card" title="查询条件与数据录入">
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={14}>
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
                    { value: "humidity", label: "湿度" }
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
          </Col>

          <Col xs={24} xl={10}>
            <div className="action-row">
              <Button type="primary">新增采样数据</Button>
              <Button>导入历史数据</Button>
              <Button>下载模板</Button>
            </div>
          </Col>
        </Row>
      </Card>

      <Row gutter={[16, 16]}>
        {environmentOverviewCards.map((item) => (
          <Col xs={24} md={8} key={item.label}>
            <Card className="panel-card">
              <Space direction="vertical" size={8}>
                <Text strong>{item.label}</Text>
                <div className="summary-value">{item.value}</div>
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

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card className="panel-card" title="温湿度趋势图">
            <MultiLineTrendChart series={environmentTrendSeries} />
          </Card>
        </Col>

        <Col xs={24} xl={9}>
          <Card className="panel-card" title="重点指标对比">
            <RankingBarChart items={environmentMetricRanking} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card className="panel-card" title="环境数据记录">
            <Table rowKey="id" columns={dataColumns} dataSource={environmentRows} pagination={{ pageSize: 6 }} />
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card className="panel-card" title="导入与预处理说明">
            <div className="compact-list">
              {dataImportSteps.map((item) => (
                <div className="compact-list-item" key={item}>
                  {item}
                </div>
              ))}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
