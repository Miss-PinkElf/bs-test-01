import { AppShell } from "@/components/AppShell";
import { PredictionPage } from "@/components/pages/PredictionPage";

export default function PredictionRoute() {
  return (
    <AppShell pageTitle="温度预测" pageDescription="展示历史温度、预测曲线、风险提示和预测归档效果。">
      <PredictionPage />
    </AppShell>
  );
}
