import { useAuth } from "@/hooks/useAuth";
import { useQuery } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PawPrint, MessageSquare, Stethoscope, Users } from "lucide-react";

export default function Dashboard() {
  const { role } = useAuth();

  const { data: petCount = 0 } = useQuery({
    queryKey: ["dashboard-pets"],
    queryFn: async () => {
      const { count } = await supabase.from("pets").select("*", { count: "exact", head: true });
      return count ?? 0;
    },
  });

  const { data: activeRequests = 0 } = useQuery({
    queryKey: ["dashboard-requests"],
    queryFn: async () => {
      const { count } = await supabase
        .from("treatment_requests")
        .select("*", { count: "exact", head: true })
        .in("status", ["submitted", "assigned", "in_progress"]);
      return count ?? 0;
    },
  });

  const { data: prescriptionCount = 0 } = useQuery({
    queryKey: ["dashboard-prescriptions"],
    queryFn: async () => {
      const startOfMonth = new Date();
      startOfMonth.setDate(1);
      startOfMonth.setHours(0, 0, 0, 0);
      const { count } = await supabase
        .from("prescriptions")
        .select("*", { count: "exact", head: true })
        .gte("created_at", startOfMonth.toISOString());
      return count ?? 0;
    },
  });

  const { data: userCount = 0 } = useQuery({
    queryKey: ["dashboard-users"],
    enabled: role === "admin",
    queryFn: async () => {
      const { count } = await supabase.from("user_roles").select("*", { count: "exact", head: true });
      return count ?? 0;
    },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {role === "vet" ? "Vet Dashboard" : role === "admin" ? "Admin Dashboard" : "My Dashboard"}
        </h1>
        <p className="text-muted-foreground">
          Welcome to HealthyTom — your digital veterinary clinic.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Pets</CardTitle>
            <PawPrint className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{petCount}</div>
            <p className="text-xs text-muted-foreground">Registered animals</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Consultations</CardTitle>
            <MessageSquare className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{activeRequests}</div>
            <p className="text-xs text-muted-foreground">Active requests</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Prescriptions</CardTitle>
            <Stethoscope className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{prescriptionCount}</div>
            <p className="text-xs text-muted-foreground">Issued this month</p>
          </CardContent>
        </Card>
        {role === "admin" && (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Users</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{userCount}</div>
              <p className="text-xs text-muted-foreground">Registered users</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
