import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { consultationApi, prescriptionApi, petApi } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { ArrowLeft, Pill, Stethoscope, PawPrint } from "lucide-react";
import { format } from "date-fns";

const statusColors: Record<string, string> = {
  PENDING: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
  submitted: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
  SCHEDULED: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
  assigned: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
  IN_PROGRESS: "bg-primary/15 text-primary",
  in_progress: "bg-primary/15 text-primary",
  COMPLETED: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
  completed: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
  CANCELLED: "bg-muted text-muted-foreground",
  cancelled: "bg-muted text-muted-foreground",
};

export default function ConsultationDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, role } = useAuth();
  const queryClient = useQueryClient();
  const consultationId = Number(id);

  const [showPrescriptionForm, setShowPrescriptionForm] = useState(false);
  const [medicationName, setMedicationName] = useState("");
  const [dosage, setDosage] = useState("");
  const [instructions, setInstructions] = useState("");
  const [duration, setDuration] = useState("");
  const [diagnosis, setDiagnosis] = useState("");

  const { data: request, isLoading } = useQuery({
    queryKey: ["treatment-request", consultationId],
    queryFn: () => consultationApi.getById(consultationId),
    enabled: !!consultationId,
  });

  const { data: pet } = useQuery({
    queryKey: ["pet", request?.petId],
    queryFn: () => petApi.getById(request!.petId),
    enabled: !!request?.petId,
  });

  const { data: prescriptions } = useQuery({
    queryKey: ["prescriptions", consultationId],
    queryFn: () => prescriptionApi.getByConsultation(consultationId),
    enabled: !!consultationId,
  });

  const statusMutation = useMutation({
    mutationFn: async (newStatus: string) => {
      await consultationApi.update(consultationId, { status: newStatus });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["treatment-request", consultationId] });
      queryClient.invalidateQueries({ queryKey: ["treatment-requests"] });
      toast.success("Status updated");
    },
    onError: () => toast.error("Failed to update status"),
  });

  const prescriptionMutation = useMutation({
    mutationFn: async () => {
      if (!user || !request) throw new Error("Missing data");
      await prescriptionApi.create({
        consultationId,
        petId: request.petId,
        veterinarianId: user.id,
        medicationName,
        dosage,
        instructions: instructions || null,
        duration: duration ? Number(duration) : null,
        notes: diagnosis || null,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["prescriptions", consultationId] });
      toast.success("Prescription issued!");
      setShowPrescriptionForm(false);
      setMedicationName("");
      setDosage("");
      setInstructions("");
      setDuration("");
      setDiagnosis("");
    },
    onError: (err: any) => toast.error(err.message || "Failed to issue prescription"),
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!request) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Request not found</p>
        <Button variant="ghost" onClick={() => navigate(-1)} className="mt-4">Go Back</Button>
      </div>
    );
  }

  const isVet = role === "vet";
  const isActive = !["COMPLETED", "CANCELLED", "completed", "cancelled"].includes(request.status);

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <Button variant="ghost" onClick={() => navigate(isVet ? "/requests" : "/consultations")} className="gap-2">
        <ArrowLeft className="h-4 w-4" /> Back
      </Button>

      {/* Request Details */}
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between flex-wrap gap-4">
            <div className="flex items-center gap-4">
              {pet?.photoUrl ? (
                <img src={pet.photoUrl} alt={pet.name} className="h-14 w-14 rounded-xl object-cover border-2 border-primary/20" />
              ) : (
                <div className="h-14 w-14 rounded-xl bg-primary/10 flex items-center justify-center">
                  <PawPrint className="h-6 w-6 text-primary" />
                </div>
              )}
              <div>
                <CardTitle className="text-xl flex items-center gap-2">
                  Treatment Request
                  <Badge className={statusColors[request.status]}>{request.status.replace("_", " ")}</Badge>
                </CardTitle>
                <CardDescription className="flex items-center gap-2 mt-1">
                  {pet?.name || "Pet"} ({pet?.species || ""}{pet?.breed ? ` · ${pet.breed}` : ""})
                  {request.consultationDate && (
                    <span className="text-xs">· {format(new Date(request.consultationDate), "MMM d, yyyy")}</span>
                  )}
                </CardDescription>
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label className="text-xs text-muted-foreground">Symptoms</Label>
            <p className="mt-1">{request.symptoms}</p>
          </div>
          {request.notes && (
            <div>
              <Label className="text-xs text-muted-foreground">Additional Notes</Label>
              <p className="mt-1 text-sm">{request.notes}</p>
            </div>
          )}

          {/* Vet Actions */}
          {isVet && isActive && (
            <div className="flex gap-2 pt-2 flex-wrap">
              {["PENDING", "submitted"].includes(request.status) && (
                <Button size="sm" onClick={() => statusMutation.mutate("SCHEDULED")}>
                  Accept & Assign to Me
                </Button>
              )}
              {["SCHEDULED", "assigned"].includes(request.status) && (
                <Button size="sm" onClick={() => statusMutation.mutate("IN_PROGRESS")}>
                  Start Treatment
                </Button>
              )}
              {["IN_PROGRESS", "SCHEDULED", "in_progress", "assigned"].includes(request.status) && (
                <>
                  <Button size="sm" variant="outline" onClick={() => setShowPrescriptionForm(true)}>
                    <Pill className="h-4 w-4 mr-2" /> Issue Prescription
                  </Button>
                  <Button size="sm" variant="secondary" onClick={() => statusMutation.mutate("COMPLETED")}>
                    Mark Completed
                  </Button>
                </>
              )}
            </div>
          )}

          {/* View Pet Link */}
          <Button variant="link" className="p-0" onClick={() => navigate(`/pets/${request.petId}`)}>
            View Pet Profile →
          </Button>
        </CardContent>
      </Card>

      {/* Prescription Form */}
      {showPrescriptionForm && (
        <Card className="border-primary/30">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <Stethoscope className="h-5 w-5" /> New Prescription
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={(e) => { e.preventDefault(); prescriptionMutation.mutate(); }}
              className="space-y-4"
            >
              <div className="space-y-2">
                <Label htmlFor="diagnosis">Diagnosis</Label>
                <Textarea id="diagnosis" value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} placeholder="Clinical diagnosis..." rows={2} />
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="medication">Medication *</Label>
                  <Input id="medication" value={medicationName} onChange={(e) => setMedicationName(e.target.value)} required placeholder="e.g. Amoxicillin" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="dosage">Dosage *</Label>
                  <Input id="dosage" value={dosage} onChange={(e) => setDosage(e.target.value)} required placeholder="e.g. 250mg twice daily" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="duration">Duration (days)</Label>
                  <Input id="duration" type="number" value={duration} onChange={(e) => setDuration(e.target.value)} placeholder="e.g. 7" />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="instructions">Instructions</Label>
                <Textarea id="instructions" value={instructions} onChange={(e) => setInstructions(e.target.value)} placeholder="Administration instructions..." rows={2} />
              </div>
              <div className="flex gap-3">
                <Button type="submit" disabled={prescriptionMutation.isPending}>
                  {prescriptionMutation.isPending ? "Issuing..." : "Issue Prescription"}
                </Button>
                <Button type="button" variant="outline" onClick={() => setShowPrescriptionForm(false)}>
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Prescriptions List */}
      {prescriptions && prescriptions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <Pill className="h-5 w-5" /> Prescriptions
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {prescriptions.map((rx) => (
              <div key={rx.id} className="border rounded-lg p-4 space-y-2">
                {rx.notes && (
                  <div>
                    <span className="text-xs text-muted-foreground">Diagnosis:</span>
                    <p className="text-sm font-medium">{rx.notes}</p>
                  </div>
                )}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div>
                    <span className="text-xs text-muted-foreground">Medication</span>
                    <p className="text-sm font-semibold">{rx.medicationName}</p>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">Dosage</span>
                    <p className="text-sm">{rx.dosage}</p>
                  </div>
                  {rx.duration && (
                    <div>
                      <span className="text-xs text-muted-foreground">Duration</span>
                      <p className="text-sm">{rx.duration} days</p>
                    </div>
                  )}
                  {rx.prescribedDate && (
                    <div>
                      <span className="text-xs text-muted-foreground">Issued</span>
                      <p className="text-sm">{format(new Date(rx.prescribedDate), "MMM d, yyyy")}</p>
                    </div>
                  )}
                </div>
                {rx.instructions && (
                  <div>
                    <span className="text-xs text-muted-foreground">Instructions:</span>
                    <p className="text-sm">{rx.instructions}</p>
                  </div>
                )}
              </div>
            ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
