import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/hooks/useAuth";
import { AppLayout } from "@/components/AppLayout";
import Auth from "./pages/Auth";
import ResetPassword from "./pages/ResetPassword";
import Dashboard from "./pages/Dashboard";
import PetList from "./pages/PetList";
import PetForm from "./pages/PetForm";
import PetProfile from "./pages/PetProfile";
import NewRequest from "./pages/NewRequest";
import Consultations from "./pages/Consultations";
import ConsultationDetail from "./pages/ConsultationDetail";
import Prescriptions from "./pages/Prescriptions";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { session, loading } = useAuth();
  if (loading) return <div className="flex min-h-screen items-center justify-center"><div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" /></div>;
  if (!session) return <Navigate to="/auth" replace />;
  return <>{children}</>;
}

function AuthRoute({ children }: { children: React.ReactNode }) {
  const { session, loading } = useAuth();
  if (loading) return null;
  if (session) return <Navigate to="/" replace />;
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/auth" element={<AuthRoute><Auth /></AuthRoute>} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/pets" element={<PetList />} />
        <Route path="/pets/new" element={<PetForm />} />
        <Route path="/pets/:id" element={<PetProfile />} />
        <Route path="/requests/new" element={<NewRequest />} />
        <Route path="/consultations" element={<Consultations />} />
        <Route path="/consultations/:id" element={<ConsultationDetail />} />
        <Route path="/profile" element={<Dashboard />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/requests" element={<Consultations />} />
        <Route path="/prescriptions" element={<Prescriptions />} />
        <Route path="/admin/users" element={<Dashboard />} />
        <Route path="/admin/logs" element={<Dashboard />} />
        <Route path="/admin/stats" element={<Dashboard />} />
        <Route path="/admin/settings" element={<Dashboard />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
