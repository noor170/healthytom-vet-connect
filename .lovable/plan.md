

# HealthyTom — Digital Veterinary Clinic

A full-stack veterinary health platform for farmers, pet owners, vets, and admins — built entirely on Lovable Cloud (Supabase).

---

## 🎨 Design & Theme
- **Color palette**: Emerald green (#059669) + soft white, with clean medical-grade UI
- **Typography**: Modern, readable sans-serif
- **Mobile-first**: Fully responsive for farmers in the field
- **Component library**: Shadcn/UI throughout

---

## 🔐 Phase 1: Authentication & Roles

### Auth System
- Email/password sign-up and login with Supabase Auth
- Role selection during registration (Farmer/Owner or Vet Doctor)
- Admin role assigned manually via database
- Password reset flow with dedicated `/reset-password` page

### Role-Based Access Control
- **Roles table** (separate from profiles, using `app_role` enum): `farmer`, `vet`, `admin`
- Security definer function (`has_role`) for RLS policies
- UI elements conditionally rendered based on role
- Protected routes per role

### User Profiles
- Profile table linked to `auth.users` with auto-creation trigger
- Fields: name, phone, location, avatar, role-specific info

---

## 🐾 Phase 2: Pet Profiles ("Digital Health Cards")

### Pet Registration
- Add pets with: name, species (Cow, Goat, Cat, Dog), breed, age, weight, photo
- Each pet gets a unique "Digital Health Card" page

### Photo Gallery
- Supabase Storage bucket for pet photos
- Upload photos with descriptions for symptom tracking
- Gallery view on each pet's profile

### Health Timeline
- Chronological record of treatments, prescriptions, and uploaded photos
- Clean timeline UI showing past visits and notes

---

## 💬 Phase 3: Consultation System

### Treatment Requests
- Farmers can submit a "Treatment Request" for a pet
- Include symptoms description, urgency level, and attached photos
- Request is routed to available vets

### Vet Dashboard
- Vets see assigned/incoming treatment requests
- View pet profile, photo gallery, and health history
- Issue digital prescriptions (medication name, dosage, instructions, duration)

### Real-Time Updates
- Supabase real-time subscriptions for request status changes
- Status flow: Submitted → Assigned → In Progress → Completed
- Notifications via toast/sonner for new requests and responses

---

## 🛡️ Phase 4: Admin Dashboard

### User Management
- View all users with role, status, and registration date
- Ability to assign/revoke vet or admin roles
- Deactivate accounts

### Audit Logs
- Track key actions: logins, treatment requests, prescriptions issued
- Filterable log table with timestamps, user, and action type

### System Overview
- Stats cards: total users, active consultations, pets registered
- Charts showing consultation volume over time

---

## 📱 Navigation & Layout

### Role-Based Navigation
- **Farmer**: My Pets, New Request, My Consultations, Profile
- **Vet**: Dashboard, Assigned Requests, Prescriptions, Profile
- **Admin**: Users, Audit Logs, System Stats, Settings

### Responsive Sidebar
- Collapsible sidebar on desktop
- Bottom navigation or hamburger menu on mobile

---

## 🗄️ Database Tables (Supabase)
- `profiles` — user details
- `user_roles` — RBAC (separate from profiles)
- `pets` — animal profiles
- `pet_photos` — photo metadata (files in Supabase Storage)
- `treatment_requests` — consultation requests
- `prescriptions` — vet-issued prescriptions
- `health_records` — timeline entries
- `audit_logs` — admin audit trail

---

## Implementation Order
1. **Auth + Roles + Profiles** → secure foundation
2. **Pet Profiles + Photo Upload** → core value
3. **Treatment Requests + Vet Dashboard** → consultation flow
4. **Admin Dashboard + Audit Logs** → management layer
5. **Polish** → real-time updates, mobile optimization, health timeline

