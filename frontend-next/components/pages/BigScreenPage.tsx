"use client";

import Link from "next/link";
import { Button, Card, Space, Tag, Typography } from "antd";
import {
  processStages,
  screenAlerts,
  screenHealthRanking,
  screenMetricCards,
  screenPredictionBoard,
  screenTrendSeries,
  screenWarehousePanels
} from "@/mock/grain-data";
import { MultiLineTrendChart } from "@/components/charts/MultiLineTrendChart";
import { RankingBarChart } from "@/components/charts/RankingBarChart";

const { Text } = Typography;

export function BigScreenPage() {
  return (
    <div className="screen-page">
      <div className="screen-header">
        <div>
          <div className="screen-eyebrow">答辩展示大屏</div>
          <h1 className="screen-title">粮仓环境数据预测管理平台</h1>
          <div className="screen-subtitle">把仓库态势、环境趋势、温度预测和风险提示聚合到一屏展示。</div>
        </div>

        <Space>
          <Tag color="processing">静态演示</Tag>
          <Tag color="gold">更新时间 2026-04-06 10:30</Tag>
          <Button type="primary" ghost>
            <Link href="/dashboard">返回后台</Link>
          </Button>
        </Space>
      </div>

      <div className="screen-stat-grid">
        {screenMetricCards.map((item) => (
          <Card className="screen-stat-card" key={item.key} variant="borderless">
            <div className="screen-stat-label">{item.label}</div>
            <div className="screen-stat-value">{item.value}</div>
            <div className="screen-stat-note">{item.note}</div>
          </Card>
        ))}
      </div>

      <div className="screen-grid">
        <Card className="screen-panel screen-panel-tall" title="粮仓运行态势" variant="borderless">
          <RankingBarChart items={screenHealthRanking} theme="dark" />
        </Card>

        <Card className="screen-panel screen-panel-wide" title="环境趋势与预测走势" variant="borderless">
          <MultiLineTrendChart series={screenTrendSeries} unit="" theme="dark" height={300} />
        </Card>

        <Card className="screen-panel screen-panel-tall" title="重点预警" variant="borderless">
          <div className="screen-list">
            {screenAlerts.map((item) => (
              <div className="screen-alert-item" key={item.title}>
                <div className="screen-alert-title">
                  <span>{item.title}</span>
                  <Tag color={item.level === "高" ? "red" : item.level === "中" ? "gold" : "blue"}>{item.level}</Tag>
                </div>
                <div className="screen-alert-description">{item.description}</div>
              </div>
            ))}
          </div>
        </Card>

        <Card className="screen-panel" title="重点仓库卡片" variant="borderless">
          <div className="screen-card-grid">
            {screenWarehousePanels.map((item) => (
              <div className="screen-warehouse-card" key={item.warehouseName}>
                <div className="timeline-title">{item.warehouseName}</div>
                <div className="screen-warehouse-metrics">
                  <span>温度 {item.temperature}</span>
                  <span>湿度 {item.humidity}</span>
                </div>
                <div className="screen-warehouse-note">{item.trend}</div>
                <Tag color={item.riskLabel === "高风险" ? "red" : item.riskLabel === "中风险" ? "gold" : "green"}>
                  {item.riskLabel}
                </Tag>
              </div>
            ))}
          </div>
        </Card>

        <Card className="screen-panel" title="预测调度看板" variant="borderless">
          <div className="screen-list">
            {screenPredictionBoard.map((item) => (
              <div className="screen-board-item" key={item.warehouseName}>
                <div className="screen-board-title">{item.warehouseName}</div>
                <div className="screen-board-meta">
                  <span>峰值时间 {item.nextPeakTime}</span>
                  <span>峰值 {item.peakValue}</span>
                </div>
                <div className="screen-board-action">{item.action}</div>
              </div>
            ))}
          </div>
        </Card>

        <Card className="screen-panel" title="系统主流程" variant="borderless">
          <div className="screen-stage-list">
            {processStages.map((item, index) => (
              <div className="screen-stage-item" key={item.title}>
                <div className="screen-stage-index">{index + 1}</div>
                <div>
                  <div className="timeline-title">{item.title}</div>
                  <div className="screen-stage-description">{item.description}</div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
