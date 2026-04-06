import { TrendSeries } from "@/mock/grain-data";

type MultiLineTrendChartProps = {
  series: TrendSeries[];
  unit?: string;
  height?: number;
  theme?: "light" | "dark";
};

const SVG_WIDTH = 640;

function buildPolylinePoints(
  values: number[],
  chartWidth: number,
  chartHeight: number,
  minValue: number,
  maxValue: number
) {
  const horizontalGap = values.length > 1 ? chartWidth / (values.length - 1) : chartWidth;
  const valueRange = Math.max(maxValue - minValue, 1);

  return values
    .map((value, index) => {
      const x = index * horizontalGap;
      const y = chartHeight - ((value - minValue) / valueRange) * chartHeight;
      return `${x},${y}`;
    })
    .join(" ");
}

export function MultiLineTrendChart({
  series,
  unit,
  height = 260,
  theme = "light"
}: MultiLineTrendChartProps) {
  const isDark = theme === "dark";
  const chartPadding = {
    top: 18,
    right: 18,
    bottom: 40,
    left: 42
  };

  const allPoints = series.flatMap((item) => item.points.map((point) => point.value));
  const minPointValue = Math.min(...allPoints);
  const maxPointValue = Math.max(...allPoints);
  const valuePadding = Math.max((maxPointValue - minPointValue) * 0.12, 1);
  const chartMinValue = minPointValue - valuePadding;
  const chartMaxValue = maxPointValue + valuePadding;
  const chartWidth = SVG_WIDTH - chartPadding.left - chartPadding.right;
  const chartHeight = height - chartPadding.top - chartPadding.bottom;
  const labels = series[0]?.points.map((point) => point.label) ?? [];
  const gridValues = Array.from({ length: 4 }, (_, index) => {
    const percent = index / 3;
    return chartMaxValue - (chartMaxValue - chartMinValue) * percent;
  });
  const gridColor = isDark ? "rgba(148, 163, 184, 0.18)" : "#dbe5f3";
  const axisTextColor = isDark ? "#cbd5e1" : "#64748b";

  return (
    <div className={`trend-chart ${isDark ? "trend-chart-dark" : ""}`}>
      <div className="trend-legend">
        {series.map((item) => (
          <span className="trend-legend-item" key={item.key}>
            <span className="trend-legend-swatch" style={{ background: item.color }} />
            {item.label}
          </span>
        ))}
      </div>

      <svg viewBox={`0 0 ${SVG_WIDTH} ${height}`} className="trend-chart-svg" role="img" aria-label="趋势图">
        <g transform={`translate(${chartPadding.left}, ${chartPadding.top})`}>
          {gridValues.map((gridValue, index) => {
            const y = (chartHeight / (gridValues.length - 1)) * index;

            return (
              <g key={gridValue}>
                <line x1={0} y1={y} x2={chartWidth} y2={y} stroke={gridColor} strokeDasharray="4 6" />
                <text x={-12} y={y + 4} textAnchor="end" fill={axisTextColor} fontSize="12">
                  {`${gridValue.toFixed(0)}${unit ?? ""}`}
                </text>
              </g>
            );
          })}

          {labels.map((label, index) => {
            const x = labels.length > 1 ? (chartWidth / (labels.length - 1)) * index : chartWidth / 2;

            return (
              <text key={label} x={x} y={chartHeight + 26} textAnchor="middle" fill={axisTextColor} fontSize="12">
                {label}
              </text>
            );
          })}

          {series.map((item) => {
            const points = buildPolylinePoints(
              item.points.map((point) => point.value),
              chartWidth,
              chartHeight,
              chartMinValue,
              chartMaxValue
            );

            return (
              <g key={item.key}>
                <polyline
                  fill="none"
                  stroke={item.color}
                  strokeWidth="3"
                  strokeLinejoin="round"
                  strokeLinecap="round"
                  strokeDasharray={item.dashed ? "8 8" : undefined}
                  points={points}
                />
                {item.points.map((point, index) => {
                  const x = item.points.length > 1 ? (chartWidth / (item.points.length - 1)) * index : chartWidth / 2;
                  const valueRange = Math.max(chartMaxValue - chartMinValue, 1);
                  const y = chartHeight - ((point.value - chartMinValue) / valueRange) * chartHeight;

                  return (
                    <g key={`${item.key}-${point.label}`}>
                      <circle cx={x} cy={y} r="4" fill={item.color} stroke={isDark ? "#0f172a" : "#ffffff"} strokeWidth="2" />
                      <text x={x} y={y - 10} textAnchor="middle" fill={axisTextColor} fontSize="11">
                        {`${point.value.toFixed(1)}${unit ?? ""}`}
                      </text>
                    </g>
                  );
                })}
              </g>
            );
          })}
        </g>
      </svg>
    </div>
  );
}
