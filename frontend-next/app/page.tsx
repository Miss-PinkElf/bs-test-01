import { AppShell } from "@/components/AppShell";
import { DashboardPage } from "@/components/pages/DashboardPage";

export default function HomePage() {
  return (
    <AppShell pageTitle="仪表盘" pageDescription="查看粮仓概览、采样趋势、预警信息和最终成品的主流程预期。">
      <DashboardPage />
    </AppShell>
  );
}
