import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/AppSidebar";
import { MobileBottomNav } from "@/components/MobileBottomNav";
import { useRealtimeNotifications } from "@/hooks/useRealtimeNotifications";
import { useWebPush } from "@/hooks/useWebPush";
import { useAuth } from "@/hooks/useAuth";
import { Outlet } from "react-router-dom";
import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";

export function AppLayout() {
  useRealtimeNotifications();
  const { permission, requestPermission } = useWebPush();
  const { role } = useAuth();

  return (
    <SidebarProvider>
      <div className="min-h-screen flex w-full">
        {/* Sidebar hidden on mobile, shown on md+ */}
        <div className="hidden md:block">
          <AppSidebar />
        </div>
        <main className="flex-1 flex flex-col">
          <header className="hidden md:flex items-center h-14 border-b px-4 bg-background/80 backdrop-blur-sm sticky top-0 z-10">
            <SidebarTrigger />
          </header>
          {/* Mobile header */}
          <header className="flex md:hidden items-center justify-between h-12 border-b px-4 bg-background/95 backdrop-blur-sm sticky top-0 z-10">
            <span className="text-lg font-bold text-primary tracking-tight">HealthyTom</span>
            {role === "vet" && permission !== "granted" && (
              <Button variant="ghost" size="icon" onClick={requestPermission} title="Enable push notifications">
                <Bell className="h-4 w-4" />
              </Button>
            )}
          </header>
          <div className="flex-1 p-4 md:p-6 lg:p-8 pb-20 md:pb-8">
            <Outlet />
          </div>
          <MobileBottomNav />
        </main>
      </div>
    </SidebarProvider>
  );
}
