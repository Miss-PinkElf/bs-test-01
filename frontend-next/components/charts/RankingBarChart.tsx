import { RankingItem } from "@/mock/grain-data";

type RankingBarChartProps = {
  items: RankingItem[];
  theme?: "light" | "dark";
};

export function RankingBarChart({ items, theme = "light" }: RankingBarChartProps) {
  const isDark = theme === "dark";
  const maxValue = Math.max(...items.map((item) => item.value), 1);

  return (
    <div className={`ranking-chart ${isDark ? "ranking-chart-dark" : ""}`}>
      {items.map((item) => {
        const widthPercent = Math.max((item.value / maxValue) * 100, 8);

        return (
          <div className="ranking-row" key={item.key}>
            <div className="ranking-meta">
              <div className="ranking-label-row">
                <span className="ranking-label">{item.label}</span>
                <span className="ranking-value">
                  {item.value}
                  {item.suffix ?? ""}
                </span>
              </div>
              <div className="ranking-note">{item.note}</div>
            </div>
            <div className="ranking-track">
              <div className="ranking-fill" style={{ width: `${widthPercent}%`, background: item.color }} />
            </div>
          </div>
        );
      })}
    </div>
  );
}
