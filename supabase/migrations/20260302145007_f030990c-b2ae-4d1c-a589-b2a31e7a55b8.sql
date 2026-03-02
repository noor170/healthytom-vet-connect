
-- Create audit_logs table
CREATE TABLE public.audit_logs (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID,
  action TEXT NOT NULL,
  entity_type TEXT,
  entity_id UUID,
  details JSONB,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can view all audit logs" ON public.audit_logs FOR SELECT USING (public.has_role(auth.uid(), 'admin'));
CREATE POLICY "System can insert audit logs" ON public.audit_logs FOR INSERT WITH CHECK (true);

-- Create index for faster queries
CREATE INDEX idx_audit_logs_created_at ON public.audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON public.audit_logs(action);

-- Create a function to log audit events
CREATE OR REPLACE FUNCTION public.log_audit_event(
  _user_id UUID,
  _action TEXT,
  _entity_type TEXT DEFAULT NULL,
  _entity_id UUID DEFAULT NULL,
  _details JSONB DEFAULT NULL
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  INSERT INTO public.audit_logs (user_id, action, entity_type, entity_id, details)
  VALUES (_user_id, _action, _entity_type, _entity_id, _details);
END;
$$;

-- Add trigger to log treatment request status changes
CREATE OR REPLACE FUNCTION public.log_treatment_request_change()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    PERFORM public.log_audit_event(NEW.farmer_id, 'treatment_request_created', 'treatment_request', NEW.id, jsonb_build_object('status', NEW.status, 'urgency', NEW.urgency));
  ELSIF TG_OP = 'UPDATE' AND OLD.status IS DISTINCT FROM NEW.status THEN
    PERFORM public.log_audit_event(COALESCE(NEW.vet_id, NEW.farmer_id), 'treatment_request_status_changed', 'treatment_request', NEW.id, jsonb_build_object('old_status', OLD.status, 'new_status', NEW.status));
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER audit_treatment_requests
AFTER INSERT OR UPDATE ON public.treatment_requests
FOR EACH ROW
EXECUTE FUNCTION public.log_treatment_request_change();

-- Add trigger to log prescription creation
CREATE OR REPLACE FUNCTION public.log_prescription_created()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  PERFORM public.log_audit_event(NEW.vet_id, 'prescription_issued', 'prescription', NEW.id, jsonb_build_object('medication', NEW.medication, 'pet_id', NEW.pet_id));
  RETURN NEW;
END;
$$;

CREATE TRIGGER audit_prescriptions
AFTER INSERT ON public.prescriptions
FOR EACH ROW
EXECUTE FUNCTION public.log_prescription_created();
