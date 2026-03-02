import { useAuth } from "@/hooks/useAuth";
import { useQuery } from "@tanstack/react-query";
import { petApi, consultationApi, prescriptionApi } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PawPrint, MessageSquare, Stethoscope, Users } from "lucide-react";

export default function Dashboard() {
  const { role, user } = useAuth();

  const { data: petCount = 0 } = useQuery({
    queryKey: ["dashboard-pets"],
    queryFn: async () => {
      if (!user) return 0;
      const pets =
        role === "farmer"
          ? await petApi.getByOwner(user.id)
          : await petApi.getAll();
      return pets.length;
    },
    enabled: !!user,
  });

  const { data: activeRequests = 0 } = useQuery({
    queryKey: ["dashboard-requests"],
    queryFn: async () => {
      if (!user) return 0;
      let consultations;
      if (role === "farmer") {
        consultations = await consultationApi.getByOwner(user.id);
      } else if (role === "vet") {
        consultations = await consultationApi.getByVet(user.id);
      } else {
        consultations = await consultationApi.getAll();
      }
      return consultations.filter((c) =>
        ["PENDING", "SCHEDULED", "IN_PROGRESS", "submitted", "assigned", "in_progress"].includes(c.status),
      ).length;
    },
    enabled: !!user,
  });

  const { data: prescriptionCount = 0 } = useQuery({
    queryKey: ["dashboard-prescriptions"],
    queryFn: async () => {
      if (!user) return 0;
      let prescriptions;
      if (role === "vet") {
        prescriptions = await prescriptionApi.getByVet(user.id);
      } else {
        prescriptions = await prescriptionApi.getAll().catch(() => []);
      }
      return prescriptions.length;
    },
    enabled: !!user,
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
            <p className="text-xs text-muted-foreground">Total issued</p>
          </CardContent>
        </Card>
        {role === "admin" && (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Users</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">—</div>
              <p className="text-xs text-muted-foreground">Registered users</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
