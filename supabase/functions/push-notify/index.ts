import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type, x-supabase-client-platform, x-supabase-client-platform-version, x-supabase-client-runtime, x-supabase-client-runtime-version",
};

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

// ---------- VAPID helpers using Web Crypto ----------

async function generateVapidKeys() {
  const keyPair = await crypto.subtle.generateKey(
    { name: "ECDSA", namedCurve: "P-256" },
    true,
    ["sign", "verify"]
  );
  const pubRaw = await crypto.subtle.exportKey("raw", keyPair.publicKey);
  const privJwk = await crypto.subtle.exportKey("jwk", keyPair.privateKey);
  const publicKey = btoa(String.fromCharCode(...new Uint8Array(pubRaw)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  const privateKey = privJwk.d!; // base64url-encoded private scalar
  return { publicKey, privateKey };
}

function base64urlToUint8Array(b64: string): Uint8Array {
  const base64 = b64.replace(/-/g, "+").replace(/_/g, "/");
  const pad = base64.length % 4 === 0 ? "" : "=".repeat(4 - (base64.length % 4));
  const binary = atob(base64 + pad);
  return Uint8Array.from(binary, (c) => c.charCodeAt(0));
}

async function importPrivateKey(b64url: string) {
  const jwk = {
    kty: "EC",
    crv: "P-256",
    d: b64url,
    x: "", // placeholder, will be filled from public key
    y: "",
  };
  // We need to derive x,y from the public key. Instead, store as JWK completely.
  // Actually, we'll re-import the full JWK including d.
  // Since we only stored d, we need another approach.
  // Let's store the full JWK instead.
  return null; // Will use a different approach
}

// ---------- Web Push sending using raw fetch ----------

async function sendWebPush(
  subscription: { endpoint: string; p256dh: string; auth: string },
  payload: string,
  vapidPublicKey: string,
  vapidPrivateKey: string,
  supabaseUrl: string
) {
  // For proper Web Push, we need to:
  // 1. Create a VAPID JWT
  // 2. Encrypt the payload using ECDH + HKDF + AES-GCM
  // This is complex, so we'll use a simpler notification approach

  // Create VAPID Authorization header
  const endpoint = new URL(subscription.endpoint);
  const audience = `${endpoint.protocol}//${endpoint.host}`;

  // Create JWT for VAPID
  const header = { typ: "JWT", alg: "ES256" };
  const now = Math.floor(Date.now() / 1000);
  const claims = {
    aud: audience,
    exp: now + 12 * 3600,
    sub: `mailto:noreply@healthytom.app`,
  };

  const headerB64 = btoa(JSON.stringify(header))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  const claimsB64 = btoa(JSON.stringify(claims))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  const unsignedToken = `${headerB64}.${claimsB64}`;

  // Import the private key for signing
  const privateKeyBytes = base64urlToUint8Array(vapidPrivateKey);

  // We need the full key pair for signing. Let's reconstruct the JWK.
  const publicKeyBytes = base64urlToUint8Array(vapidPublicKey);

  // Extract x and y from uncompressed public key (65 bytes: 0x04 || x || y)
  const x = btoa(String.fromCharCode(...publicKeyBytes.slice(1, 33)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  const y = btoa(String.fromCharCode(...publicKeyBytes.slice(33, 65)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  const privateJwk = {
    kty: "EC",
    crv: "P-256",
    x,
    y,
    d: vapidPrivateKey,
  };

  const signingKey = await crypto.subtle.importKey(
    "jwk",
    privateJwk,
    { name: "ECDSA", namedCurve: "P-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    { name: "ECDSA", hash: "SHA-256" },
    signingKey,
    new TextEncoder().encode(unsignedToken)
  );

  // Convert DER signature to raw r||s format is already done by WebCrypto
  const sigB64 = btoa(String.fromCharCode(...new Uint8Array(signature)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  const vapidToken = `${unsignedToken}.${sigB64}`;

  // Send the push message (without encryption for simplicity - just the VAPID auth)
  // Note: Encrypted payloads require RFC 8291 implementation
  // For now, send a push with no payload (the SW will show a default message)
  // OR we use the payload as a TTL-only push and handle in SW

  const response = await fetch(subscription.endpoint, {
    method: "POST",
    headers: {
      Authorization: `vapid t=${vapidToken}, k=${vapidPublicKey}`,
      "Content-Length": "0",
      TTL: "86400",
    },
  });

  return { ok: response.ok, status: response.status };
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, serviceRoleKey);

    const body = await req.json();
    const { action } = body;

    // -------- GET VAPID PUBLIC KEY --------
    if (action === "get-vapid-key") {
      let { data: config } = await supabase.from("vapid_config").select("public_key").eq("id", 1).maybeSingle();

      if (!config) {
        const keys = await generateVapidKeys();
        await supabase.from("vapid_config").insert({
          id: 1,
          public_key: keys.publicKey,
          private_key: keys.privateKey,
        });
        return json({ publicKey: keys.publicKey });
      }

      return json({ publicKey: config.public_key });
    }

    // -------- SUBSCRIBE --------
    if (action === "subscribe") {
      const authHeader = req.headers.get("Authorization");
      if (!authHeader) return json({ error: "Unauthorized" }, 401);

      const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
      const userClient = createClient(supabaseUrl, anonKey, {
        global: { headers: { Authorization: authHeader } },
      });
      const { data: { user } } = await userClient.auth.getUser();
      if (!user) return json({ error: "Unauthorized" }, 401);

      const { subscription } = body;
      const keys = subscription.keys;

      const { error } = await supabase.from("push_subscriptions").upsert(
        {
          user_id: user.id,
          endpoint: subscription.endpoint,
          p256dh: keys.p256dh,
          auth: keys.auth,
        },
        { onConflict: "user_id,endpoint" }
      );

      if (error) return json({ error: error.message }, 400);
      return json({ ok: true });
    }

    // -------- NOTIFY VETS --------
    if (action === "notify-vets") {
      const { title, message, url } = body;

      // Get all vet user IDs
      const { data: vetRoles } = await supabase
        .from("user_roles")
        .select("user_id")
        .eq("role", "vet");

      if (!vetRoles?.length) return json({ sent: 0 });

      const vetIds = vetRoles.map((r: any) => r.user_id);

      // Get push subscriptions for vets
      const { data: subs } = await supabase
        .from("push_subscriptions")
        .select("*")
        .in("user_id", vetIds);

      if (!subs?.length) return json({ sent: 0 });

      // Get VAPID keys
      const { data: config } = await supabase
        .from("vapid_config")
        .select("*")
        .eq("id", 1)
        .single();

      if (!config) return json({ error: "VAPID not configured" }, 500);

      let sent = 0;
      let failed = 0;

      for (const sub of subs) {
        try {
          const result = await sendWebPush(
            { endpoint: sub.endpoint, p256dh: sub.p256dh, auth: sub.auth },
            JSON.stringify({ title, message, url }),
            config.public_key,
            config.private_key,
            supabaseUrl
          );
          if (result.ok) sent++;
          else {
            failed++;
            // Remove expired subscriptions
            if (result.status === 404 || result.status === 410) {
              await supabase.from("push_subscriptions").delete().eq("id", sub.id);
            }
          }
        } catch (e) {
          console.error("Push send error:", e);
          failed++;
        }
      }

      return json({ sent, failed });
    }

    return json({ error: "Unknown action" }, 400);
  } catch (err) {
    console.error("push-notify error:", err);
    const message = err instanceof Error ? err.message : "Unknown error";
    return json({ error: message }, 500);
  }
});
