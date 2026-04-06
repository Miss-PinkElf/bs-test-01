"use client";

import { Alert, Button, Card, Col, Progress, Row, Select, Space, Table, Tag, Typography } from "antd";
import {
  forecastSummary,
  predictionArchiveRecords,
  predictionColumnsData,
  predictionInsightItems,
  predictionRecords,
  predictionTrendSeries
} from "@/mock/grain-data";
import { MultiLineTrendChart } from "@/components/charts/MultiLineTrendChart";

const { Paragraph, Text } = Typography;

const predictionColumns = [
  { title: "预测时间", dataIndex: "time", key: "time" },
  { title: "实际值", dataIndex: "actualValue", key: "actualValue" },
  { title: "预测值", dataIndex: "predictedValue", key: "predictedValue" },
  { title: "置信度", dataIndex: "confidence", key: "confidence" },
  {
    title: "风险判断",
    dataIndex: "statusLabel",
    key: "statusLabel",
    render: (statusLabel: string, record: (typeof predictionRecords)[number]) => (
      <Tag color={record.statusColor}>{statusLabel}</Tag>
    )
  }
];

export function PredictionPage() {
  return (
    <div className="section-stack">
      <Card className="panel-card" title="预测参数">
        <div className="action-row action-row-spread">
          <Space wrap size={16}>
            <Select
              defaultValue="WH-A01"
              style={{ width: 180 }}
              options={[
                { value: "WH-A01", label: "一号粮仓" },
                { value: "WH-B02", label: "二号粮仓" },
                { value: "WH-C03", label: "三号粮仓" }
              ]}
            />
            <Select
              defaultValue="temperature"
              style={{ width: 160 }}
              options={[{ value: "temperature", label: "温度" }]}
            />
            <Select
              defaultValue="6"
              style={{ width: 180 }}
              options={[6, 12, 24].map((step) => ({ value: String(step), label: `未来 ${step} 小时` }))}
            />
            <Tag color="blue">算法：线性回归（mock）</Tag>
          </Space>

          <Button type="primary">执行预测</Button>
        </div>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card className="panel-card" title="历史温度与预测曲线">
            <MultiLineTrendChart series={predictionTrendSeries} unit=" °C" />
          </Card>
        </Col>

        <Col xs={24} xl={9}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Card className="panel-card" title="预测摘要">
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                {predictionColumnsData.map((item) => (
                  <div key={item.label}>
                    <Text strong>{item.label}</Text>
                    <div style={{ marginTop: 6 }}>{item.value}</div>
                  </div>
                ))}
              </Space>
            </Card>

            <Card className="panel-card" title="任务状态">
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                {predictionInsightItems.map((item) => (
                  <div key={item.label}>
                    <Text strong>{item.label}</Text>
                    <div style={{ marginTop: 6, fontSize: 22, fontWeight: 700 }}>{item.value}</div>
                    <div className="mini-text">{item.note}</div>
                  </div>
                ))}
              </Space>
            </Card>
          </Space>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card className="panel-card" title="预测结果列表">
            <Table rowKey="time" columns={predictionColumns} dataSource={predictionRecords} pagination={false} />
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Card className="panel-card" title="未来温度走势">
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                {forecastSummary.map((item) => (
                  <div key={item.time}>
                    <Space style={{ width: "100%", justifyContent: "space-between" }}>
                      <span>{item.time}</span>
                      <Tag color={item.statusColor}>{item.temperature}</Tag>
                    </Space>
                    <Progress percent={item.percent} showInfo={false} strokeColor="#2f6fed" />
                  </div>
                ))}
              </Space>
            </Card>

            <Card className="panel-card" title="历史归档记录">
              <div className="compact-list">
                {predictionArchiveRecords.map((item) => (
                  <div className="compact-list-item" key={item.id}>
                    <div className="timeline-title">{item.taskName}</div>
                    <div className="mini-text">
                      {item.createdAt} · {item.algorithmName}
                    </div>
                    <div style={{ marginTop: 6 }}>{item.summary}</div>
                  </div>
                ))}
              </div>
            </Card>
          </Space>
        </Col>
      </Row>

      <Alert
        type="warning"
        showIcon
        message="静态原型说明"
        description={
          <Paragraph style={{ margin: 0 }}>
            正式版后端只需要把历史数据读取、预测算法执行、结果保存三段逻辑接入接口，这个页面就能从 mock 过渡到真实业务。
          </Paragraph>
        }
      />
    </div>
  );
}
