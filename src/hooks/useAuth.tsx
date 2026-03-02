import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import {
  authApi,
  userApi,
  setTokens,
  clearTokens,
  getAccessToken,
  type UserDto,
} from "@/lib/api";

type AppRole = "farmer" | "vet" | "admin";

function mapBackendRole(role: string): AppRole {
  switch (role?.toUpperCase()) {
    case "VETERINARIAN":
      return "vet";
    case "ADMIN":
      return "admin";
    case "OWNER":
    default:
      return "farmer";
  }
}

function mapFrontendRoleToBackend(role: AppRole): string {
  switch (role) {
    case "vet":
      return "VETERINARIAN";
    case "admin":
      return "ADMIN";
    case "farmer":
    default:
      return "OWNER";
  }
}

interface AuthContextType {
  user: UserDto | null;
  role: AppRole | null;
  loading: boolean;
  session: { access_token: string } | null; // minimal compat
  signUp: (email: string, password: string, fullName: string, role: AppRole) => Promise<{ error: any }>;
  signIn: (email: string, password: string) => Promise<{ error: any }>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [role, setRole] = useState<AppRole | null>(null);
  const [loading, setLoading] = useState(true);

  // On mount, check if we have a stored token and load user
  useEffect(() => {
    const token = getAccessToken();
    if (token) {
      userApi
        .getMe()
        .then((u) => {
          setUser(u);
          setRole(mapBackendRole(u.role));
        })
        .catch(() => {
          clearTokens();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const signUp = async (
    email: string,
    password: string,
    fullName: string,
    selectedRole: AppRole,
  ) => {
    try {
      const nameParts = fullName.trim().split(" ");
      const firstName = nameParts[0] || "";
      const lastName = nameParts.slice(1).join(" ") || "";

      const res = await authApi.register({
        email,
        password,
        firstName,
        lastName,
        role: mapFrontendRoleToBackend(selectedRole),
      });

      setTokens(res.accessToken, res.refreshToken);
      setUser(res.user);
      setRole(mapBackendRole(res.user.role));
      return { error: null };
    } catch (err: any) {
      return { error: { message: err.message || "Registration failed" } };
    }
  };

  const signIn = async (email: string, password: string) => {
    try {
      const res = await authApi.login(email, password);
      setTokens(res.accessToken, res.refreshToken);
      setUser(res.user);
      setRole(mapBackendRole(res.user.role));
      return { error: null };
    } catch (err: any) {
      return { error: { message: err.message || "Login failed" } };
    }
  };

  const signOut = async () => {
    clearTokens();
    setUser(null);
    setRole(null);
  };

  const session = user ? { access_token: getAccessToken()! } : null;

  return (
    <AuthContext.Provider value={{ user, role, loading, session, signUp, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}
