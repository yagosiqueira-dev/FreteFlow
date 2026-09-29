import { createContext, useState, type ReactNode } from "react";
import { setToken as setStoredToken } from "../api/tokenStore";

interface AuthContextType {
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);

  function login(newToken: string) {
    setStoredToken(newToken);
    setToken(newToken);
  }

  function logout() {
    setStoredToken(null);
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}