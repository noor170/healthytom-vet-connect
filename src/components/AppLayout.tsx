import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/AppSidebar";
import { MobileBottomNav } from "@/components/MobileBottomNav";
import { useRealtimeNotifications } from "@/hooks/useRealtimeNotifications";
import { Outlet } from "react-router-dom";

export function AppLayout() {
  useRealtimeNotifications();

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
          <header className="flex md:hidden items-center h-12 border-b px-4 bg-background/95 backdrop-blur-sm sticky top-0 z-10">
            <span className="text-lg font-bold text-primary tracking-tight">HealthyTom</span>
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
