import { AppShell } from "@/components/AppShell";
import { DashboardPage } from "@/components/pages/DashboardPage";

export default function DashboardRoute() {
  return (
    <AppShell pageTitle="仪表盘" pageDescription="查看粮仓概览、预警信息和近期预测摘要。">
      <DashboardPage />
    </AppShell>
  );
}
