/**
 * API client for the Spring Boot backend.
 * All data flows through this module instead of Supabase.
 */

const API_BASE = "http://localhost:8080";

// ── Token helpers ──────────────────────────────────────────────

export function getAccessToken(): string | null {
  return localStorage.getItem("access_token");
}

export function getRefreshToken(): string | null {
  return localStorage.getItem("refresh_token");
}

export function setTokens(access: string, refresh: string) {
  localStorage.setItem("access_token", access);
  localStorage.setItem("refresh_token", refresh);
}

export function clearTokens() {
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
}

// ── Generic fetch wrapper ──────────────────────────────────────

async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getAccessToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401) {
    // Try refresh once
    const refreshed = await tryRefreshToken();
    if (refreshed) {
      headers["Authorization"] = `Bearer ${getAccessToken()}`;
      const retry = await fetch(`${API_BASE}${path}`, { ...options, headers });
      if (!retry.ok) throw new Error(await retry.text());
      if (retry.status === 204) return undefined as T;
      return retry.json();
    }
    clearTokens();
    window.location.href = "/auth";
    throw new Error("Session expired");
  }

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

async function tryRefreshToken(): Promise<boolean> {
  const refresh = getRefreshToken();
  if (!refresh) return false;
  try {
    const res = await fetch(`${API_BASE}/auth/refresh-token`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: refresh }),
    });
    if (!res.ok) return false;
    const data: AuthResponse = await res.json();
    setTokens(data.token, data.refreshToken);
    return true;
  } catch {
    return false;
  }
}

// ── Types ──────────────────────────────────────────────────────

export interface UserDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  profileImageUrl: string | null;
  role: string; // OWNER, VETERINARIAN, ADMIN
  emailVerified: boolean;
  specialization: string | null;
  licenseNumber: string | null;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: UserDto;
}

export interface PetDto {
  id: number;
  name: string;
  species: string;
  breed: string | null;
  color: string | null;
  weight: number | null;
  dateOfBirth: string | null;
  microchipNumber: string | null;
  medicalRecordNumber: string | null;
  medicalHistory: string | null;
  vaccinations: string | null;
  photoUrl: string | null;
  ownerId: number;
}

export interface ConsultationDto {
  id: number;
  petId: number;
  ownerId: number;
  veterinarianId: number | null;
  title: string | null;
  description: string | null;
  symptoms: string;
  diagnosis: string | null;
  notes: string | null;
  status: string;
  consultationDate: string;
  completedAt: string | null;
  rating: number | null;
  feedback: string | null;
  // Enriched by frontend
  petName?: string;
  petSpecies?: string;
}

export interface PrescriptionDto {
  id: number;
  consultationId: number;
  petId: number;
  veterinarianId: number;
  medicationName: string;
  dosage: string;
  frequency: string | null;
  duration: number | null;
  instructions: string | null;
  sideEffects: string | null;
  status: string | null;
  prescribedDate: string;
  startDate: string | null;
  endDate: string | null;
  notes: string | null;
  // Enriched by frontend
  petName?: string;
  petSpecies?: string;
}

// ── Auth API ───────────────────────────────────────────────────

export const authApi = {
  register(data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    role: string;
    specialization?: string;
    licenseNumber?: string;
  }) {
    return apiFetch<AuthResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  login(email: string, password: string) {
    return apiFetch<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  refreshToken(refreshToken: string) {
    return apiFetch<AuthResponse>("/auth/refresh-token", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
  },
};

// ── User API ───────────────────────────────────────────────────

export const userApi = {
  getMe() {
    return apiFetch<UserDto>("/users/me");
  },
  getById(id: number) {
    return apiFetch<UserDto>(`/users/${id}`);
  },
};

// ── Pet API ────────────────────────────────────────────────────

export const petApi = {
  getAll() {
    return apiFetch<PetDto[]>("/pets");
  },
  getByOwner(ownerId: number) {
    return apiFetch<PetDto[]>(`/pets/owner/${ownerId}`);
  },
  getById(id: number) {
    return apiFetch<PetDto>(`/pets/${id}`);
  },
  create(pet: Partial<PetDto>, ownerId: number) {
    return apiFetch<PetDto>(`/pets?ownerId=${ownerId}`, {
      method: "POST",
      body: JSON.stringify(pet),
    });
  },
  update(id: number, pet: Partial<PetDto>) {
    return apiFetch<PetDto>(`/pets/${id}`, {
      method: "PUT",
      body: JSON.stringify(pet),
    });
  },
  delete(id: number) {
    return apiFetch<void>(`/pets/${id}`, { method: "DELETE" });
  },
};

// ── Consultation API ───────────────────────────────────────────

export const consultationApi = {
  getAll() {
    return apiFetch<ConsultationDto[]>("/consultations");
  },
  getByOwner(ownerId: number) {
    return apiFetch<ConsultationDto[]>(`/consultations/owner/${ownerId}`);
  },
  getByVet(vetId: number) {
    return apiFetch<ConsultationDto[]>(`/consultations/veterinarian/${vetId}`);
  },
  getByPet(petId: number) {
    return apiFetch<ConsultationDto[]>(`/consultations/pet/${petId}`);
  },
  getById(id: number) {
    return apiFetch<ConsultationDto>(`/consultations/${id}`);
  },
  create(consultation: Partial<ConsultationDto>, ownerId: number) {
    return apiFetch<ConsultationDto>(`/consultations?ownerId=${ownerId}`, {
      method: "POST",
      body: JSON.stringify(consultation),
    });
  },
  update(id: number, consultation: Partial<ConsultationDto>) {
    return apiFetch<ConsultationDto>(`/consultations/${id}`, {
      method: "PUT",
      body: JSON.stringify(consultation),
    });
  },
  assignVet(id: number, vetId: number) {
    return apiFetch<ConsultationDto>(
      `/consultations/${id}/assign-veterinarian/${vetId}`,
      { method: "PUT" },
    );
  },
  delete(id: number) {
    return apiFetch<void>(`/consultations/${id}`, { method: "DELETE" });
  },
};

// ── Prescription API ───────────────────────────────────────────

export const prescriptionApi = {
  getAll() {
    return apiFetch<PrescriptionDto[]>("/prescriptions");
  },
  getByConsultation(consultationId: number) {
    return apiFetch<PrescriptionDto[]>(
      `/prescriptions/consultation/${consultationId}`,
    );
  },
  getByPet(petId: number) {
    return apiFetch<PrescriptionDto[]>(`/prescriptions/pet/${petId}`);
  },
  getByVet(vetId: number) {
    return apiFetch<PrescriptionDto[]>(
      `/prescriptions/veterinarian/${vetId}`,
    );
  },
  getById(id: number) {
    return apiFetch<PrescriptionDto>(`/prescriptions/${id}`);
  },
  create(prescription: Partial<PrescriptionDto>) {
    return apiFetch<PrescriptionDto>("/prescriptions", {
      method: "POST",
      body: JSON.stringify(prescription),
    });
  },
  update(id: number, prescription: Partial<PrescriptionDto>) {
    return apiFetch<PrescriptionDto>(`/prescriptions/${id}`, {
      method: "PUT",
      body: JSON.stringify(prescription),
    });
  },
  delete(id: number) {
    return apiFetch<void>(`/prescriptions/${id}`, { method: "DELETE" });
  },
};
