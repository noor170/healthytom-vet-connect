import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { prescriptionApi, petApi } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Pill } from "lucide-react";
import { format } from "date-fns";

export default function Prescriptions() {
  const { role, user } = useAuth();
  const navigate = useNavigate();

  const { data: prescriptions, isLoading } = useQuery({
    queryKey: ["all-prescriptions"],
    queryFn: async () => {
      if (!user) return [];
      let rxList;
      if (role === "vet") {
        rxList = await prescriptionApi.getByVet(user.id);
      } else {
        rxList = await prescriptionApi.getAll().catch(() => []);
      }

      // Enrich with pet names
      const petIds = [...new Set(rxList.map((r) => r.petId))] as number[];
      const petMap = new Map<number, { name: string; species: string }>();
      await Promise.all(
        petIds.map(async (pid) => {
          try {
            const pet = await petApi.getById(pid);
            petMap.set(pid, { name: pet.name, species: pet.species });
          } catch {
            /* ignore */
          }
        }),
      );

      return rxList.map((rx) => ({
        ...rx,
        petName: petMap.get(rx.petId)?.name || "Unknown",
        petSpecies: petMap.get(rx.petId)?.species || "",
      }));
    },
    enabled: !!user,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Prescriptions</h1>
        <p className="text-muted-foreground">
          {role === "vet" ? "Prescriptions you've issued" : "Prescriptions for your pets"}
        </p>
      </div>

      {!prescriptions?.length ? (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Pill className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-1">No prescriptions yet</h3>
            <p className="text-muted-foreground">Prescriptions will appear here after consultations</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {prescriptions.map((rx) => (
            <Card
              key={rx.id}
              className="cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => navigate(`/consultations/${rx.consultationId}`)}
            >
              <CardContent className="py-4">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Pill className="h-4 w-4 text-primary" />
                    <span className="font-semibold">{rx.medicationName}</span>
                    <Badge variant="outline" className="text-xs">
                      {rx.petName} — {rx.petSpecies}
                    </Badge>
                  </div>
                  {rx.prescribedDate && (
                    <span className="text-xs text-muted-foreground">
                      {format(new Date(rx.prescribedDate), "MMM d, yyyy")}
                    </span>
                  )}
                </div>
                <div className="flex gap-4 text-sm text-muted-foreground">
                  <span>Dosage: {rx.dosage}</span>
                  {rx.duration && <span>Duration: {rx.duration} days</span>}
                </div>
                {rx.notes && (
                  <p className="text-sm mt-1">Diagnosis: {rx.notes}</p>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
