import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import {
  PawPrint,
  Plus,
  MessageSquare,
  User,
  LayoutDashboard,
  ClipboardList,
  Pill,
  Users,
  BarChart3,
} from "lucide-react";

const farmerNav = [
  { title: "Pets", icon: PawPrint, path: "/pets" },
  { title: "New", icon: Plus, path: "/requests/new" },
  { title: "Consults", icon: MessageSquare, path: "/consultations" },
  { title: "Profile", icon: User, path: "/profile" },
];

const vetNav = [
  { title: "Dashboard", icon: LayoutDashboard, path: "/dashboard" },
  { title: "Requests", icon: ClipboardList, path: "/requests" },
  { title: "Scripts", icon: Pill, path: "/prescriptions" },
  { title: "Profile", icon: User, path: "/profile" },
];

const adminNav = [
  { title: "Users", icon: Users, path: "/admin/users" },
  { title: "Stats", icon: BarChart3, path: "/admin/stats" },
  { title: "Profile", icon: User, path: "/profile" },
];

export function MobileBottomNav() {
  const { role } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = role === "admin" ? adminNav : role === "vet" ? vetNav : farmerNav;

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t bg-background/95 backdrop-blur-md md:hidden">
      <div className="flex items-center justify-around h-16 px-2 pb-safe">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path ||
            (item.path !== "/" && location.pathname.startsWith(item.path));
          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`flex flex-col items-center justify-center gap-0.5 flex-1 py-1.5 rounded-lg transition-colors ${
                isActive
                  ? "text-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <item.icon className={`h-5 w-5 ${isActive ? "stroke-[2.5]" : ""}`} />
              <span className="text-[10px] font-medium leading-tight">{item.title}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
