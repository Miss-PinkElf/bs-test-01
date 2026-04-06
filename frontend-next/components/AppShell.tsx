"use client";

import { Avatar, Button, Layout, Menu, Space, Tag, Typography } from "antd";
import {
  AlertOutlined,
  AreaChartOutlined,
  BarChartOutlined,
  DatabaseOutlined,
  HomeOutlined,
  LogoutOutlined,
  ShopOutlined,
  TeamOutlined,
  UserOutlined
} from "@ant-design/icons";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { MOCK_CURRENT_USER } from "@/mock/grain-data";

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const NAV_ITEMS = [
  { key: "/dashboard", icon: <HomeOutlined />, label: <Link href="/dashboard">仪表盘</Link> },
  { key: "/users", icon: <TeamOutlined />, label: <Link href="/users">用户管理</Link> },
  { key: "/warehouses", icon: <ShopOutlined />, label: <Link href="/warehouses">仓库管理</Link> },
  { key: "/environment", icon: <DatabaseOutlined />, label: <Link href="/environment">环境数据</Link> },
  { key: "/prediction", icon: <BarChartOutlined />, label: <Link href="/prediction">温度预测</Link> },
  { key: "/screen", icon: <AreaChartOutlined />, label: <Link href="/screen">展示大屏</Link> }
];

type AppShellProps = {
  pageTitle: string;
  pageDescription: string;
  children: React.ReactNode;
};

export function AppShell({ pageTitle, pageDescription, children }: AppShellProps) {
  const pathname = usePathname();

  return (
    <Layout className="page-shell">
      <Sider theme="light" width={252} style={{ borderRight: "1px solid #e7ebf3" }}>
        <div style={{ padding: 24 }}>
          <div style={{ fontSize: 22, fontWeight: 700 }}>粮仓平台原型</div>
          <div className="mini-text" style={{ marginTop: 8 }}>
            PRD 对齐的高保真静态演示
          </div>
        </div>
        <Menu mode="inline" selectedKeys={[pathname]} items={NAV_ITEMS} style={{ borderInlineEnd: "none" }} />
      </Sider>

      <Layout>
        <Header
          style={{
            background: "#fff",
            borderBottom: "1px solid #e7ebf3",
            padding: "0 24px",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center"
          }}
        >
          <Space align="center">
            <AlertOutlined style={{ color: "#2f6fed" }} />
            <Text className="mini-text">当前为 frontend-next 静态参考原型，正式前端仍然是 Vue 3 路线</Text>
          </Space>

          <Space size={12}>
            <Tag color="blue">{MOCK_CURRENT_USER.roleLabel}</Tag>
            <Avatar icon={<UserOutlined />} />
            <span>{MOCK_CURRENT_USER.displayName}</span>
            <Button icon={<LogoutOutlined />} href="/login">
              退出
            </Button>
          </Space>
        </Header>

        <Content className="page-container">
          <div className="page-header">
            <div>
              <h1 className="page-title">{pageTitle}</h1>
              <div className="page-subtitle">{pageDescription}</div>
            </div>
            <Tag color="processing">高保真静态原型</Tag>
          </div>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
