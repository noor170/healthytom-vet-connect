import { useEffect } from "react";
import { supabase } from "@/integrations/supabase/client";
import { useAuth } from "@/hooks/useAuth";
import { toast } from "sonner";

/**
 * Listens for new treatment_requests via Supabase Realtime.
 * Shows a toast to vets (and admins) when a new request is submitted.
 */
export function useRealtimeNotifications() {
  const { role, user } = useAuth();

  useEffect(() => {
    if (!user || (role !== "vet" && role !== "admin")) return;

    const channel = supabase
      .channel("new-treatment-requests")
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "treatment_requests" },
        async (payload) => {
          const req = payload.new as {
            id: string;
            symptoms: string;
            urgency: string;
            pet_id: string;
          };

          // Fetch pet name for a richer notification
          const { data: pet } = await supabase
            .from("pets")
            .select("name, species")
            .eq("id", req.pet_id)
            .maybeSingle();

          const petLabel = pet ? `${pet.name} (${pet.species})` : "a pet";
          const urgencyLabel = req.urgency.charAt(0).toUpperCase() + req.urgency.slice(1);

          toast.info(`New ${urgencyLabel} request for ${petLabel}`, {
            description: req.symptoms.length > 80 ? req.symptoms.slice(0, 80) + "…" : req.symptoms,
            duration: 8000,
            action: {
              label: "View",
              onClick: () => {
                window.location.href = `/consultations/${req.id}`;
              },
            },
          });
        }
      )
      .on(
        "postgres_changes",
        { event: "UPDATE", schema: "public", table: "treatment_requests" },
        (payload) => {
          const oldRow = payload.old as { status?: string };
          const newRow = payload.new as { id: string; status: string; pet_id: string };

          if (oldRow.status !== newRow.status) {
            toast.info(`Request status changed to ${newRow.status}`, {
              duration: 5000,
            });
          }
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [user, role]);
}
