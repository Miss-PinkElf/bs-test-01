import { AppShell } from "@/components/AppShell";
import { WarehousesPage } from "@/components/pages/WarehousesPage";

export default function WarehousesRoute() {
  return (
    <AppShell pageTitle="仓库管理" pageDescription="查看粮仓档案、负责人、容量和运行状态。">
      <WarehousesPage />
    </AppShell>
  );
}
