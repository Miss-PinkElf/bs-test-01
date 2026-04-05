import { AppShell } from "@/components/AppShell";
import { EnvironmentPage } from "@/components/pages/EnvironmentPage";

export default function EnvironmentRoute() {
  return (
    <AppShell pageTitle="环境数据" pageDescription="查看温度、湿度等环境记录，全部使用 mock 数据展示。">
      <EnvironmentPage />
    </AppShell>
  );
}
