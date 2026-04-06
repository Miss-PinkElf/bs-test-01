import { AppShell } from "@/components/AppShell";
import { EnvironmentPage } from "@/components/pages/EnvironmentPage";

export default function EnvironmentRoute() {
  return (
    <AppShell pageTitle="环境数据" pageDescription="展示数据录入、导入、查询、图表分析和预处理说明。">
      <EnvironmentPage />
    </AppShell>
  );
}
