import { createContext, useState } from "react";
import type { ReactNode } from "react";
import { decodeJwtRole } from "../utils/jwt";

interface AuthContextType {
  token: string | null;
  role: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [role, setRole] = useState<string | null>(() => {
    const stored = localStorage.getItem("token");
    return stored ? decodeJwtRole(stored) : null;
  });

  function login(newToken: string) {
    localStorage.setItem("token", newToken);
    setToken(newToken);
    setRole(decodeJwtRole(newToken));
  }

  function logout() {
    localStorage.removeItem("token");
    setToken(null);
    setRole(null);
  }

  return (
    <AuthContext.Provider value={{ token, role, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}