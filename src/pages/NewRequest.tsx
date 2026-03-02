import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { ArrowLeft, AlertTriangle } from "lucide-react";

const URGENCY_OPTIONS = [
  { value: "low", label: "Low — Routine checkup", color: "text-muted-foreground" },
  { value: "medium", label: "Medium — Needs attention soon", color: "text-warning" },
  { value: "high", label: "High — Urgent care needed", color: "text-destructive" },
  { value: "emergency", label: "Emergency — Immediate help!", color: "text-destructive" },
] as const;

export default function NewRequest() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [petId, setPetId] = useState("");
  const [urgency, setUrgency] = useState<string>("medium");
  const [symptoms, setSymptoms] = useState("");
  const [notes, setNotes] = useState("");

  const { data: pets } = useQuery({
    queryKey: ["pets"],
    queryFn: async () => {
      const { data, error } = await supabase.from("pets").select("id, name, species").order("name");
      if (error) throw error;
      return data;
    },
  });

  const mutation = useMutation({
    mutationFn: async () => {
      if (!user) throw new Error("Not authenticated");
      const { data, error } = await supabase.from("treatment_requests").insert({
        pet_id: petId,
        farmer_id: user.id,
        urgency: urgency as any,
        symptoms,
        notes: notes || null,
      }).select("id").single();
      if (error) throw error;

      // Trigger push notifications to vets (fire and forget)
      const selectedPet = pets?.find((p) => p.id === petId);
      const petLabel = selectedPet ? `${selectedPet.name} (${selectedPet.species})` : "a pet";
      supabase.functions.invoke("push-notify", {
        body: {
          action: "notify-vets",
          title: `🐾 New ${urgency} request`,
          message: `Treatment needed for ${petLabel}: ${symptoms.slice(0, 100)}`,
          url: `/consultations/${data.id}`,
        },
      }).catch(console.error);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["treatment-requests"] });
      toast.success("Treatment request submitted!");
      navigate("/consultations");
    },
    onError: (err: any) => toast.error(err.message || "Failed to submit request"),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!petId) { toast.error("Please select a pet"); return; }
    if (!symptoms.trim()) { toast.error("Please describe the symptoms"); return; }
    mutation.mutate();
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Button variant="ghost" onClick={() => navigate("/consultations")} className="gap-2">
        <ArrowLeft className="h-4 w-4" /> Back to Consultations
      </Button>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5" /> New Treatment Request
          </CardTitle>
          <CardDescription>Describe your pet's symptoms and a vet will be assigned</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Pet *</Label>
              <Select value={petId} onValueChange={setPetId}>
                <SelectTrigger><SelectValue placeholder="Select your pet" /></SelectTrigger>
                <SelectContent>
                  {pets?.map((pet) => (
                    <SelectItem key={pet.id} value={pet.id}>
                      {pet.name} ({pet.species})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {!pets?.length && (
                <p className="text-xs text-muted-foreground">
                  No pets registered.{" "}
                  <Button variant="link" className="p-0 h-auto text-xs" onClick={() => navigate("/pets/new")}>
                    Add a pet first
                  </Button>
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label>Urgency *</Label>
              <Select value={urgency} onValueChange={setUrgency}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {URGENCY_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="symptoms">Symptoms *</Label>
              <Textarea
                id="symptoms"
                value={symptoms}
                onChange={(e) => setSymptoms(e.target.value)}
                placeholder="Describe what you've observed — behaviour changes, physical symptoms, appetite, etc."
                rows={4}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="notes">Additional Notes</Label>
              <Textarea
                id="notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Any other relevant information..."
                rows={2}
              />
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? "Submitting..." : "Submit Request"}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate("/consultations")}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
