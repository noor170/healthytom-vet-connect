import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { userApi } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { toast } from "sonner";
import { Save, User, Phone, Mail } from "lucide-react";

export default function Profile() {
  const { user, role } = useAuth();

  const fullName = user ? `${user.firstName || ""} ${user.lastName || ""}`.trim() : "";
  const roleLabel = role === "admin" ? "Admin" : role === "vet" ? "Veterinarian" : "Farmer / Owner";
  const initials = fullName
    ? fullName.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2)
    : user?.email?.charAt(0).toUpperCase() || "U";

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">My Profile</h1>
        <p className="text-muted-foreground">Your personal information</p>
      </div>

      {/* Avatar Section */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center gap-6">
            <Avatar className="h-24 w-24 border-4 border-primary/20">
              <AvatarImage src={user?.profileImageUrl || undefined} alt={fullName} />
              <AvatarFallback className="text-2xl bg-primary/10 text-primary font-bold">
                {initials}
              </AvatarFallback>
            </Avatar>
            <div>
              <h2 className="text-xl font-semibold">{fullName || "No name set"}</h2>
              <p className="text-sm text-muted-foreground">{user?.email}</p>
              <span className="inline-block mt-1 text-xs px-2 py-0.5 rounded-full bg-primary/10 text-primary font-medium">
                {roleLabel}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Details */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Personal Information</CardTitle>
          <CardDescription>Your account details from the system</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label className="flex items-center gap-2">
              <Mail className="h-4 w-4 text-muted-foreground" /> Email
            </Label>
            <Input value={user?.email || ""} disabled className="bg-muted" />
          </div>
          <div className="space-y-2">
            <Label className="flex items-center gap-2">
              <User className="h-4 w-4 text-muted-foreground" /> Full Name
            </Label>
            <Input value={fullName} disabled className="bg-muted" />
          </div>
          {user?.phoneNumber && (
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Phone className="h-4 w-4 text-muted-foreground" /> Phone
              </Label>
              <Input value={user.phoneNumber} disabled className="bg-muted" />
            </div>
          )}
          {user?.specialization && (
            <div className="space-y-2">
              <Label>Specialization</Label>
              <Input value={user.specialization} disabled className="bg-muted" />
            </div>
          )}
          {user?.licenseNumber && (
            <div className="space-y-2">
              <Label>License Number</Label>
              <Input value={user.licenseNumber} disabled className="bg-muted" />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
