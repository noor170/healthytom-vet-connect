import { useAuth } from "@/hooks/useAuth";
import { useNavigate, useLocation } from "react-router-dom";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarHeader,
  SidebarFooter,
} from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";
import {
  Heart,
  PawPrint,
  Plus,
  MessageSquare,
  User,
  LayoutDashboard,
  ClipboardList,
  Pill,
  Users,
  FileText,
  BarChart3,
  Settings,
  LogOut,
} from "lucide-react";

const farmerNav = [
  { title: "My Pets", icon: PawPrint, path: "/pets" },
  { title: "New Request", icon: Plus, path: "/requests/new" },
  { title: "My Consultations", icon: MessageSquare, path: "/consultations" },
  { title: "Profile", icon: User, path: "/profile" },
];

const vetNav = [
  { title: "Dashboard", icon: LayoutDashboard, path: "/dashboard" },
  { title: "Assigned Requests", icon: ClipboardList, path: "/requests" },
  { title: "Prescriptions", icon: Pill, path: "/prescriptions" },
  { title: "Profile", icon: User, path: "/profile" },
];

const adminNav = [
  { title: "Users", icon: Users, path: "/admin/users" },
  { title: "Audit Logs", icon: FileText, path: "/admin/logs" },
  { title: "System Stats", icon: BarChart3, path: "/admin/stats" },
  { title: "Settings", icon: Settings, path: "/admin/settings" },
];

export function AppSidebar() {
  const { role, user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = role === "admin" ? adminNav : role === "vet" ? vetNav : farmerNav;
  const roleLabel = role === "admin" ? "Admin" : role === "vet" ? "Veterinarian" : "Farmer / Owner";

  return (
    <Sidebar>
      <SidebarHeader className="p-4">
        <div className="flex items-center gap-2 text-primary">
          <Heart className="h-6 w-6 fill-primary" />
          <span className="text-lg font-bold tracking-tight">HealthyTom</span>
        </div>
        <p className="text-xs text-muted-foreground mt-1">{roleLabel}</p>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Navigation</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    isActive={location.pathname === item.path}
                    onClick={() => navigate(item.path)}
                  >
                    <item.icon className="h-4 w-4" />
                    <span>{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter className="p-4">
        <div className="text-xs text-muted-foreground truncate mb-2">
          {user?.email}
        </div>
        <Button variant="ghost" size="sm" className="w-full justify-start" onClick={signOut}>
          <LogOut className="h-4 w-4 mr-2" />
          Sign Out
        </Button>
      </SidebarFooter>
    </Sidebar>
  );
}
