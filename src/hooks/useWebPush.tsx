import { useEffect, useState } from "react";
import { supabase } from "@/integrations/supabase/client";
import { useAuth } from "@/hooks/useAuth";
import { toast } from "sonner";

function urlBase64ToUint8Array(base64String: string) {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = window.atob(base64);
  return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
}

export function useWebPush() {
  const { user, role } = useAuth();
  const [permission, setPermission] = useState<NotificationPermission>("default");

  useEffect(() => {
    if (!user || role !== "vet") return;
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) return;

    setPermission(Notification.permission);

    if (Notification.permission === "granted") {
      registerPush();
    }
  }, [user, role]);

  async function requestPermission() {
    if (!("Notification" in window)) {
      toast.error("This browser doesn't support notifications");
      return;
    }

    const result = await Notification.requestPermission();
    setPermission(result);

    if (result === "granted") {
      await registerPush();
      toast.success("Push notifications enabled!");
    } else {
      toast.error("Notification permission denied");
    }
  }

  async function registerPush() {
    try {
      const registration = await navigator.serviceWorker.register("/sw.js");
      await navigator.serviceWorker.ready;

      // Get VAPID public key from edge function
      const { data: vapidData, error: vapidError } = await supabase.functions.invoke(
        "push-notify",
        { body: { action: "get-vapid-key" } }
      );

      if (vapidError || !vapidData?.publicKey) {
        console.error("Failed to get VAPID key:", vapidError);
        return;
      }

      const applicationServerKey = urlBase64ToUint8Array(vapidData.publicKey);

      const reg = registration as any;
      let subscription = await reg.pushManager.getSubscription();

      if (!subscription) {
        subscription = await reg.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey,
        });
      }

      // Send subscription to backend
      const subJson = subscription.toJSON();
      await supabase.functions.invoke("push-notify", {
        body: {
          action: "subscribe",
          subscription: {
            endpoint: subJson.endpoint,
            keys: subJson.keys,
          },
        },
      });
    } catch (err) {
      console.error("Push registration error:", err);
    }
  }

  return { permission, requestPermission };
}
