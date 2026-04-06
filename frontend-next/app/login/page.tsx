"use client";

import { Button, Card, Checkbox, Form, Input, Space, Typography } from "antd";
import { LockOutlined, UserOutlined } from "@ant-design/icons";
import { MOCK_CURRENT_USER } from "@/mock/grain-data";

const { Paragraph, Text } = Typography;

export default function LoginPage() {
  return (
    <div className="login-wrapper">
      <Card className="login-card" variant="borderless">
        <div className="login-badge">高保真静态原型</div>
        <h1 className="login-title">粮仓环境数据预测管理平台</h1>
        <Paragraph className="login-description">
          这版页面用来提前看到最终答辩成品的样子。账号、权限、业务数据仍由本地 mock 提供，但页面结构已经按 PRD 收敛。
        </Paragraph>

        <Form layout="vertical" initialValues={{ username: MOCK_CURRENT_USER.username, password: "123456" }}>
          <Form.Item label="用户名" name="username">
            <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item label="密码" name="password">
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Space direction="vertical" size={8} style={{ width: "100%", marginBottom: 20 }}>
            <Checkbox defaultChecked>记住我</Checkbox>
            <Text className="compact-note">
              当前演示用户：{MOCK_CURRENT_USER.displayName} / {MOCK_CURRENT_USER.roleLabel}
            </Text>
          </Space>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Button type="primary" size="large" block href="/dashboard">
              进入后台原型
            </Button>
            <Button size="large" block href="/screen">
              直接查看展示大屏
            </Button>
          </Space>
        </Form>
      </Card>
    </div>
  );
}
