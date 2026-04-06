import type { Metadata } from "next";
import { AntdRegistry } from "@ant-design/nextjs-registry";
import { ConfigProvider, theme } from "antd";
import zhCN from "antd/locale/zh_CN";
import "./globals.css";

export const metadata: Metadata = {
  title: "粮仓环境数据预测管理平台",
  description: "基于 Next 与 Ant Design 的高保真静态原型，用于预览毕业设计最终成品效果"
};

type RootLayoutProps = Readonly<{
  children: React.ReactNode;
}>;

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="zh-CN">
      <body>
        <AntdRegistry>
          <ConfigProvider
            locale={zhCN}
            theme={{
              algorithm: theme.defaultAlgorithm,
              token: {
                colorPrimary: "#1677ff",
                borderRadius: 14,
                colorBgLayout: "#f5f7fb",
                fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif'
              }
            }}
          >
            {children}
          </ConfigProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}
