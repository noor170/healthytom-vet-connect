-- Table to store web push subscriptions
CREATE TABLE public.push_subscriptions (
  id uuid NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id uuid NOT NULL,
  endpoint text NOT NULL,
  p256dh text NOT NULL,
  auth text NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  UNIQUE (user_id, endpoint)
);

ALTER TABLE public.push_subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own subscriptions"
  ON public.push_subscriptions FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- Table to store auto-generated VAPID keys (single row)
CREATE TABLE public.vapid_config (
  id integer PRIMARY KEY DEFAULT 1 CHECK (id = 1),
  public_key text NOT NULL,
  private_key text NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now()
);

ALTER TABLE public.vapid_config ENABLE ROW LEVEL SECURITY;

-- Only service role can access VAPID config (no user policies)
-- The edge function uses service role to read/write this table
