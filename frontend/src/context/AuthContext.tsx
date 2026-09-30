import { createContext, useState, type ReactNode } from "react";
import { setToken as setStoredToken } from "../api/tokenStore";
import { decodeJwtRole } from "../utils/jwt";

interface AuthContextType {
  token: string | null;
  role: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);

  function login(newToken: string) {
    setStoredToken(newToken);
    setToken(newToken);
    setRole(decodeJwtRole(newToken));
  }

  function logout() {
    setStoredToken(null);
    setToken(null);
    setRole(null);
  }

  return (
    <AuthContext.Provider value={{ token, role, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}