import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Plus, MessageSquare } from "lucide-react";
import { format } from "date-fns";

const statusColors: Record<string, string> = {
  submitted: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
  assigned: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
  in_progress: "bg-primary/15 text-primary",
  completed: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
  cancelled: "bg-muted text-muted-foreground",
};

const urgencyColors: Record<string, string> = {
  low: "bg-muted text-muted-foreground",
  medium: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
  high: "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200",
  emergency: "bg-destructive/15 text-destructive",
};

export default function Consultations() {
  const navigate = useNavigate();
  const { user, role } = useAuth();

  const { data: requests, isLoading } = useQuery({
    queryKey: ["treatment-requests"],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("treatment_requests")
        .select("*, pets(name, species)")
        .order("created_at", { ascending: false });
      if (error) throw error;
      return data;
    },
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            {role === "vet" ? "Assigned Requests" : "My Consultations"}
          </h1>
          <p className="text-muted-foreground">
            {role === "vet" ? "Treatment requests from pet owners" : "Track your treatment requests"}
          </p>
        </div>
        {role === "farmer" && (
          <Button onClick={() => navigate("/requests/new")}>
            <Plus className="h-4 w-4 mr-2" /> New Request
          </Button>
        )}
      </div>

      {!requests?.length ? (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <MessageSquare className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-1">No consultations yet</h3>
            <p className="text-muted-foreground mb-4">
              {role === "vet" ? "No treatment requests have been submitted" : "Submit a treatment request for your pet"}
            </p>
            {role === "farmer" && (
              <Button onClick={() => navigate("/requests/new")}>
                <Plus className="h-4 w-4 mr-2" /> New Request
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {requests.map((req) => (
            <Card
              key={req.id}
              className="cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => navigate(`/consultations/${req.id}`)}
            >
              <CardContent className="flex items-center gap-4 py-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <h3 className="font-semibold truncate">{(req as any).pets?.name}</h3>
                    <Badge variant="outline" className="text-xs">
                      {(req as any).pets?.species}
                    </Badge>
                    <Badge className={urgencyColors[req.urgency] || ""}>
                      {req.urgency}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground truncate">{req.symptoms}</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {format(new Date(req.created_at), "MMM d, yyyy · h:mm a")}
                  </p>
                </div>
                <Badge className={statusColors[req.status] || ""}>
                  {req.status.replace("_", " ")}
                </Badge>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
