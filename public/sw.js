// HealthyTom Push Notification Service Worker

self.addEventListener("install", (event) => {
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener("push", (event) => {
  const options = {
    body: "A new treatment request needs your attention.",
    icon: "/favicon.ico",
    badge: "/favicon.ico",
    tag: "healthytom-notification",
    vibrate: [200, 100, 200],
    data: { url: "/consultations" },
    actions: [{ action: "view", title: "View Requests" }],
  };

  event.waitUntil(
    self.registration.showNotification("🐾 New Treatment Request", options)
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const url = event.notification.data?.url || "/consultations";
  event.waitUntil(
    self.clients.matchAll({ type: "window" }).then((clients) => {
      for (const client of clients) {
        if (client.url.includes(url) && "focus" in client) {
          return client.focus();
        }
      }
      return self.clients.openWindow(url);
    })
  );
});
