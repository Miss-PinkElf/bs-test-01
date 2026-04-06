import { AppShell } from "@/components/AppShell";
import { UsersPage } from "@/components/pages/UsersPage";

export default function UsersRoute() {
  return (
    <AppShell pageTitle="用户管理" pageDescription="查看系统用户、角色说明和页面级权限分配效果。">
      <UsersPage />
    </AppShell>
  );
}
