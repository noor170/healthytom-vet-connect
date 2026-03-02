
-- Create pets table
CREATE TABLE public.pets (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  owner_id UUID NOT NULL,
  name TEXT NOT NULL,
  species TEXT NOT NULL CHECK (species IN ('Cow', 'Goat', 'Cat', 'Dog')),
  breed TEXT,
  age_years NUMERIC,
  weight_kg NUMERIC,
  avatar_url TEXT,
  notes TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE public.pets ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Owners can view own pets" ON public.pets FOR SELECT USING (auth.uid() = owner_id);
CREATE POLICY "Owners can insert own pets" ON public.pets FOR INSERT WITH CHECK (auth.uid() = owner_id);
CREATE POLICY "Owners can update own pets" ON public.pets FOR UPDATE USING (auth.uid() = owner_id);
CREATE POLICY "Owners can delete own pets" ON public.pets FOR DELETE USING (auth.uid() = owner_id);
CREATE POLICY "Vets can view all pets" ON public.pets FOR SELECT USING (public.has_role(auth.uid(), 'vet'));
CREATE POLICY "Admins can view all pets" ON public.pets FOR SELECT USING (public.has_role(auth.uid(), 'admin'));

CREATE TRIGGER update_pets_updated_at BEFORE UPDATE ON public.pets FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Create pet_photos table
CREATE TABLE public.pet_photos (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  pet_id UUID NOT NULL REFERENCES public.pets(id) ON DELETE CASCADE,
  uploaded_by UUID NOT NULL,
  file_path TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE public.pet_photos ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Owners can view own pet photos" ON public.pet_photos FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.pets WHERE pets.id = pet_photos.pet_id AND pets.owner_id = auth.uid())
);
CREATE POLICY "Owners can insert pet photos" ON public.pet_photos FOR INSERT WITH CHECK (auth.uid() = uploaded_by AND
  EXISTS (SELECT 1 FROM public.pets WHERE pets.id = pet_photos.pet_id AND pets.owner_id = auth.uid())
);
CREATE POLICY "Owners can delete pet photos" ON public.pet_photos FOR DELETE USING (
  EXISTS (SELECT 1 FROM public.pets WHERE pets.id = pet_photos.pet_id AND pets.owner_id = auth.uid())
);
CREATE POLICY "Vets can view all pet photos" ON public.pet_photos FOR SELECT USING (public.has_role(auth.uid(), 'vet'));
CREATE POLICY "Admins can view all pet photos" ON public.pet_photos FOR SELECT USING (public.has_role(auth.uid(), 'admin'));

-- Create storage bucket for pet photos
INSERT INTO storage.buckets (id, name, public) VALUES ('pet-photos', 'pet-photos', true);

CREATE POLICY "Authenticated users can upload pet photos" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'pet-photos' AND auth.role() = 'authenticated');
CREATE POLICY "Anyone can view pet photos" ON storage.objects FOR SELECT USING (bucket_id = 'pet-photos');
CREATE POLICY "Users can delete own pet photos" ON storage.objects FOR DELETE USING (bucket_id = 'pet-photos' AND auth.uid()::text = (storage.foldername(name))[1]);
