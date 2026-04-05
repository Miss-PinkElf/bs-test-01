import { AppShell } from "@/components/AppShell";
import { PredictionPage } from "@/components/pages/PredictionPage";

export default function PredictionRoute() {
  return (
    <AppShell pageTitle="温度预测" pageDescription="用 mock 的历史数据和预测结果模拟最终展示效果。">
      <PredictionPage />
    </AppShell>
  );
}
