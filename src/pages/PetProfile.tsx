import { useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "sonner";
import { ArrowLeft, Camera, Trash2, Upload, Calendar } from "lucide-react";
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
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [photoDesc, setPhotoDesc] = useState("");
  const [uploading, setUploading] = useState(false);

  const { data: pet, isLoading: petLoading } = useQuery({
    queryKey: ["pet", id],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("pets")
        .select("*")
        .eq("id", id!)
        .single();
      if (error) throw error;
      return data;
    },
    enabled: !!id,
  });

  const { data: photos, isLoading: photosLoading } = useQuery({
    queryKey: ["pet-photos", id],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("pet_photos")
        .select("*")
        .eq("pet_id", id!)
        .order("created_at", { ascending: false });
      if (error) throw error;
      return data;
    },
    enabled: !!id,
  });

  const uploadMutation = useMutation({
    mutationFn: async (file: File) => {
      if (!user || !id) throw new Error("Missing data");
      setUploading(true);

      const ext = file.name.split(".").pop();
      const filePath = `${user.id}/${id}/${crypto.randomUUID()}.${ext}`;

      const { error: uploadError } = await supabase.storage
        .from("pet-photos")
        .upload(filePath, file);
      if (uploadError) throw uploadError;

      const { data: urlData } = supabase.storage
        .from("pet-photos")
        .getPublicUrl(filePath);

      const { error } = await supabase.from("pet_photos").insert({
        pet_id: id,
        uploaded_by: user.id,
        file_path: urlData.publicUrl,
        description: photoDesc || null,
      });
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pet-photos", id] });
      setPhotoDesc("");
      toast.success("Photo uploaded!");
      setUploading(false);
    },
    onError: (err: any) => {
      toast.error(err.message || "Upload failed");
      setUploading(false);
    },
  });

  const deletePhotoMutation = useMutation({
    mutationFn: async (photoId: string) => {
      const { error } = await supabase.from("pet_photos").delete().eq("id", photoId);
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pet-photos", id] });
      toast.success("Photo removed");
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) uploadMutation.mutate(file);
  };

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
            {pet.avatar_url ? (
              <img
                src={pet.avatar_url}
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
                {pet.age_years && (
                  <div className="bg-background/80 rounded-lg p-3 text-center">
                    <p className="text-xs text-muted-foreground">Age</p>
                    <p className="font-semibold text-sm">{pet.age_years} years</p>
                  </div>
                )}
                {pet.weight_kg && (
                  <div className="bg-background/80 rounded-lg p-3 text-center">
                    <p className="text-xs text-muted-foreground">Weight</p>
                    <p className="font-semibold text-sm">{pet.weight_kg} kg</p>
                  </div>
                )}
                <div className="bg-background/80 rounded-lg p-3 text-center">
                  <p className="text-xs text-muted-foreground">Registered</p>
                  <p className="font-semibold text-sm">{format(new Date(pet.created_at), "MMM d, yyyy")}</p>
                </div>
              </div>
              {pet.notes && (
                <p className="text-sm text-muted-foreground mt-4">{pet.notes}</p>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Photo Gallery */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Camera className="h-5 w-5" /> Photo Gallery
              </CardTitle>
              <CardDescription>Upload photos for symptom tracking and health records</CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Description (optional)"
                value={photoDesc}
                onChange={(e) => setPhotoDesc(e.target.value)}
                className="w-48 hidden sm:block"
              />
              <input
                type="file"
                accept="image/*"
                ref={fileInputRef}
                onChange={handleFileChange}
                className="hidden"
              />
              <Button
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                size="sm"
              >
                <Upload className="h-4 w-4 mr-2" />
                {uploading ? "Uploading..." : "Upload"}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {photosLoading ? (
            <div className="flex justify-center py-8">
              <div className="animate-spin h-6 w-6 border-4 border-primary border-t-transparent rounded-full" />
            </div>
          ) : !photos?.length ? (
            <div className="text-center py-8 text-muted-foreground">
              <Camera className="h-10 w-10 mx-auto mb-2 opacity-40" />
              <p>No photos yet. Upload symptom photos or health records.</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
              {photos.map((photo) => (
                <div key={photo.id} className="group relative rounded-lg overflow-hidden border">
                  <img
                    src={photo.file_path}
                    alt={photo.description || "Pet photo"}
                    className="w-full aspect-square object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-end p-2">
                    {photo.description && (
                      <p className="text-white text-xs mb-1">{photo.description}</p>
                    )}
                    <div className="flex items-center justify-between">
                      <span className="text-white/70 text-xs flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        {format(new Date(photo.created_at), "MMM d")}
                      </span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 text-white hover:text-destructive"
                        onClick={() => deletePhotoMutation.mutate(photo.id)}
                      >
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Health Timeline placeholder */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5" /> Health Timeline
          </CardTitle>
          <CardDescription>Treatment history and health records will appear here</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-muted-foreground">
            <p>No health records yet. Records will appear after consultations.</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
