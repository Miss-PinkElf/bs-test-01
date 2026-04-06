import { AppShell } from "@/components/AppShell";
import { WarehousesPage } from "@/components/pages/WarehousesPage";

export default function WarehousesRoute() {
  return (
    <AppShell pageTitle="仓库管理" pageDescription="查看粮仓档案、装载率、负责人和当前运行态势。">
      <WarehousesPage />
    </AppShell>
  );
}
