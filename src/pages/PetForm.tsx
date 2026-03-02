import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { petApi } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { ArrowLeft } from "lucide-react";

const SPECIES = ["Cow", "Goat", "Cat", "Dog"] as const;

export default function PetForm() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [name, setName] = useState("");
  const [species, setSpecies] = useState<string>("");
  const [breed, setBreed] = useState("");
  const [weightKg, setWeightKg] = useState("");
  const [notes, setNotes] = useState("");

  const mutation = useMutation({
    mutationFn: async () => {
      if (!user) throw new Error("Not authenticated");
      await petApi.create(
        {
          name,
          species,
          breed: breed || null,
          weight: weightKg ? Number(weightKg) : null,
          medicalHistory: notes || null,
        },
        user.id,
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pets"] });
      toast.success("Pet registered successfully!");
      navigate("/pets");
    },
    onError: (err: any) => toast.error(err.message || "Failed to register pet"),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!species) {
      toast.error("Please select a species");
      return;
    }
    mutation.mutate();
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Button variant="ghost" onClick={() => navigate("/pets")} className="gap-2">
        <ArrowLeft className="h-4 w-4" /> Back to My Pets
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Register a New Pet</CardTitle>
          <CardDescription>Create a Digital Health Card for your animal</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input id="name" value={name} onChange={(e) => setName(e.target.value)} required placeholder="e.g. Bella" />
              </div>
              <div className="space-y-2">
                <Label>Species *</Label>
                <Select value={species} onValueChange={setSpecies}>
                  <SelectTrigger><SelectValue placeholder="Select species" /></SelectTrigger>
                  <SelectContent>
                    {SPECIES.map((s) => (
                      <SelectItem key={s} value={s}>{s}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="breed">Breed</Label>
                <Input id="breed" value={breed} onChange={(e) => setBreed(e.target.value)} placeholder="e.g. Holstein" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="weight">Weight (kg)</Label>
                <Input id="weight" type="number" min="0" step="0.1" value={weightKg} onChange={(e) => setWeightKg(e.target.value)} />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="notes">Notes</Label>
              <Textarea id="notes" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Any additional information..." rows={3} />
            </div>
            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "Saving..." : "Register Pet"}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate("/pets")}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
