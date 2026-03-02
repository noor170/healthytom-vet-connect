
-- Create treatment request status enum
CREATE TYPE public.request_status AS ENUM ('submitted', 'assigned', 'in_progress', 'completed', 'cancelled');

-- Create urgency enum
CREATE TYPE public.urgency_level AS ENUM ('low', 'medium', 'high', 'emergency');

-- Create treatment_requests table
CREATE TABLE public.treatment_requests (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  pet_id UUID NOT NULL REFERENCES public.pets(id) ON DELETE CASCADE,
  farmer_id UUID NOT NULL,
  vet_id UUID,
  status request_status NOT NULL DEFAULT 'submitted',
  urgency urgency_level NOT NULL DEFAULT 'medium',
  symptoms TEXT NOT NULL,
  notes TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE public.treatment_requests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own requests" ON public.treatment_requests FOR SELECT USING (auth.uid() = farmer_id);
CREATE POLICY "Farmers can create requests" ON public.treatment_requests FOR INSERT WITH CHECK (auth.uid() = farmer_id);
CREATE POLICY "Farmers can update own requests" ON public.treatment_requests FOR UPDATE USING (auth.uid() = farmer_id);
CREATE POLICY "Vets can view all requests" ON public.treatment_requests FOR SELECT USING (public.has_role(auth.uid(), 'vet'));
CREATE POLICY "Vets can update requests" ON public.treatment_requests FOR UPDATE USING (public.has_role(auth.uid(), 'vet'));
CREATE POLICY "Admins can view all requests" ON public.treatment_requests FOR SELECT USING (public.has_role(auth.uid(), 'admin'));

CREATE TRIGGER update_treatment_requests_updated_at BEFORE UPDATE ON public.treatment_requests FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Enable realtime for treatment_requests
ALTER PUBLICATION supabase_realtime ADD TABLE public.treatment_requests;

-- Create prescriptions table
CREATE TABLE public.prescriptions (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  request_id UUID NOT NULL REFERENCES public.treatment_requests(id) ON DELETE CASCADE,
  pet_id UUID NOT NULL REFERENCES public.pets(id) ON DELETE CASCADE,
  vet_id UUID NOT NULL,
  farmer_id UUID NOT NULL,
  medication TEXT NOT NULL,
  dosage TEXT NOT NULL,
  instructions TEXT,
  duration TEXT,
  diagnosis TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE public.prescriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Farmers can view own prescriptions" ON public.prescriptions FOR SELECT USING (auth.uid() = farmer_id);
CREATE POLICY "Vets can view all prescriptions" ON public.prescriptions FOR SELECT USING (public.has_role(auth.uid(), 'vet'));
CREATE POLICY "Vets can create prescriptions" ON public.prescriptions FOR INSERT WITH CHECK (public.has_role(auth.uid(), 'vet') AND auth.uid() = vet_id);
CREATE POLICY "Admins can view all prescriptions" ON public.prescriptions FOR SELECT USING (public.has_role(auth.uid(), 'admin'));
