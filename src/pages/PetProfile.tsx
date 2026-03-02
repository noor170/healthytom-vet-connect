import { useParams, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { petApi, consultationApi } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, Calendar } from "lucide-react";
import { format } from "date-fns";

const speciesEmoji: Record<string, string> = {
  Cow: "🐄",
  Goat: "🐐",
  Cat: "🐱",
  Dog: "🐕",
};

export default function PetProfile() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const petId = Number(id);

  const { data: pet, isLoading: petLoading } = useQuery({
    queryKey: ["pet", petId],
    queryFn: () => petApi.getById(petId),
    enabled: !!petId,
  });

  const { data: consultations } = useQuery({
    queryKey: ["pet-consultations", petId],
    queryFn: () => consultationApi.getByPet(petId),
    enabled: !!petId,
  });

  if (petLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!pet) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Pet not found</p>
        <Button variant="ghost" onClick={() => navigate("/pets")} className="mt-4">
          Back to My Pets
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <Button variant="ghost" onClick={() => navigate("/pets")} className="gap-2">
        <ArrowLeft className="h-4 w-4" /> Back to My Pets
      </Button>

      {/* Health Card */}
      <Card className="overflow-hidden">
        <div className="bg-gradient-to-r from-primary/10 to-primary/5 p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row items-start gap-6">
            {pet.photoUrl ? (
              <img
                src={pet.photoUrl}
                alt={pet.name}
                className="h-24 w-24 rounded-2xl object-cover border-4 border-background shadow-lg"
              />
            ) : (
              <div className="h-24 w-24 rounded-2xl bg-background shadow-lg flex items-center justify-center text-5xl">
                {speciesEmoji[pet.species] || "🐾"}
              </div>
            )}
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h1 className="text-3xl font-bold">{pet.name}</h1>
                <Badge>{pet.species}</Badge>
              </div>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-4">
                {pet.breed && (
                  <div className="bg-background/80 rounded-lg p-3 text-center">
                    <p className="text-xs text-muted-foreground">Breed</p>
                    <p className="font-semibold text-sm">{pet.breed}</p>
                  </div>
                )}
                {pet.weight && (
                  <div className="bg-background/80 rounded-lg p-3 text-center">
                    <p className="text-xs text-muted-foreground">Weight</p>
                    <p className="font-semibold text-sm">{pet.weight} kg</p>
                  </div>
                )}
                {pet.dateOfBirth && (
                  <div className="bg-background/80 rounded-lg p-3 text-center">
                    <p className="text-xs text-muted-foreground">DOB</p>
                    <p className="font-semibold text-sm">{pet.dateOfBirth}</p>
                  </div>
                )}
              </div>
              {pet.medicalHistory && (
                <p className="text-sm text-muted-foreground mt-4">{pet.medicalHistory}</p>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Health Timeline */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5" /> Health Timeline
          </CardTitle>
          <CardDescription>Treatment history and health records</CardDescription>
        </CardHeader>
        <CardContent>
          {!consultations?.length ? (
            <div className="text-center py-8 text-muted-foreground">
              <p>No health records yet. Records will appear after consultations.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {consultations.map((c) => (
                <div
                  key={c.id}
                  className="border rounded-lg p-3 cursor-pointer hover:bg-muted/50"
                  onClick={() => navigate(`/consultations/${c.id}`)}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-sm">{c.symptoms}</span>
                    <Badge variant="outline">{c.status}</Badge>
                  </div>
                  {c.consultationDate && (
                    <p className="text-xs text-muted-foreground mt-1">
                      {format(new Date(c.consultationDate), "MMM d, yyyy")}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
