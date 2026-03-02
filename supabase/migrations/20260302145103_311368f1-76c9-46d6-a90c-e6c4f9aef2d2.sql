
-- Replace the overly permissive INSERT policy with a more restrictive one
DROP POLICY "System can insert audit logs" ON public.audit_logs;

-- Only allow inserts from authenticated users (triggers use SECURITY DEFINER so they bypass RLS)
-- This policy won't affect trigger-based inserts but prevents anonymous inserts
CREATE POLICY "Authenticated users can insert audit logs" ON public.audit_logs 
FOR INSERT WITH CHECK (auth.role() = 'authenticated');
