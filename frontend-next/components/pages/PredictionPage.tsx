"use client";

import { Alert, Card, Col, Progress, Row, Select, Space, Table, Tag, Typography } from "antd";
import { forecastSummary, predictionColumnsData, predictionRecords } from "@/mock/grain-data";

const { Paragraph, Text } = Typography;

const predictionColumns = [
  { title: "预测时间", dataIndex: "time", key: "time" },
  { title: "实际值", dataIndex: "actualValue", key: "actualValue" },
  { title: "预测值", dataIndex: "predictedValue", key: "predictedValue" },
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
          <Select defaultValue="6" style={{ width: 180 }} options={[6, 12, 24].map((step) => ({ value: String(step), label: `未来 ${step} 小时` }))} />
          <Tag color="blue">算法：线性回归（mock）</Tag>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <Card className="panel-card" title="预测结果列表">
            <Table rowKey="time" columns={predictionColumns} dataSource={predictionRecords} pagination={false} />
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

            <Card className="panel-card" title="未来温度走势">
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                {forecastSummary.map((item) => (
                  <div key={item.time}>
                    <Space style={{ width: "100%", justifyContent: "space-between" }}>
                      <span>{item.time}</span>
                      <Tag color={item.statusColor}>{item.temperature}</Tag>
                    </Space>
                    <Progress percent={item.percent} showInfo={false} strokeColor="#1677ff" />
                  </div>
                ))}
              </Space>
            </Card>

            <Alert
              type="warning"
              showIcon
              message="说明"
              description={
                <Paragraph style={{ margin: 0 }}>
                  这里先展示静态成果，后续接入真实后端时，只需要把页面数据源替换为 API 返回结果。
                </Paragraph>
              }
            />
          </Space>
        </Col>
      </Row>
    </div>
  );
}
