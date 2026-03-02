import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Heart } from "lucide-react";

export default function ResetPassword() {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardContent className="pt-6 text-center space-y-4">
          <div className="inline-flex items-center gap-2 text-primary justify-center">
            <Heart className="h-6 w-6 fill-primary" />
            <span className="text-xl font-bold">HealthyTom</span>
          </div>
          <p className="text-muted-foreground">
            Password reset is managed through the backend. Please contact your administrator.
          </p>
          <Button onClick={() => navigate("/auth")} className="w-full">
            Back to Login
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
