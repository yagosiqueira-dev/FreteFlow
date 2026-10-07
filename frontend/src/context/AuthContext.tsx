import {
  createContext,
  useCallback,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useNavigate } from "react-router-dom";
import {
  AUTH_INVALIDATED_EVENT,
  getToken,
  getTokenExpiration,
  setToken as setStoredToken,
} from "../api/tokenStore";
import { decodeJwtRole } from "../utils/jwt";

interface AuthContextType {
  token: string | null;
  role: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(() => getToken());
  const [role, setRole] = useState<string | null>(() =>
    token ? decodeJwtRole(token) : null,
  );

  function login(newToken: string) {
    setStoredToken(newToken);
    const storedToken = getToken();
    setToken(storedToken);
    setRole(storedToken ? decodeJwtRole(storedToken) : null);
  }

  const logout = useCallback(() => {
    setStoredToken(null);
    setToken(null);
    setRole(null);
  }, []);

  const expireSession = useCallback(() => {
    logout();
    navigate("/login", { replace: true });
  }, [logout, navigate]);

  useEffect(() => {
    if (!token) return;

    const expiration = getTokenExpiration(token);
    if (expiration === null || expiration <= Date.now()) {
      expireSession();
      return;
    }

    const timeout = window.setTimeout(expireSession, expiration - Date.now());
    return () => window.clearTimeout(timeout);
  }, [token, expireSession]);

  useEffect(() => {
    const handleInvalidated = () => expireSession();
    const handleStorage = (event: StorageEvent) => {
      if (event.key !== "freteflow_token") return;
      setStoredToken(event.newValue);
      const storedToken = getToken();
      setToken(storedToken);
      setRole(storedToken ? decodeJwtRole(storedToken) : null);
      if (!storedToken) navigate("/login", { replace: true });
    };

    window.addEventListener(AUTH_INVALIDATED_EVENT, handleInvalidated);
    window.addEventListener("storage", handleStorage);
    return () => {
      window.removeEventListener(AUTH_INVALIDATED_EVENT, handleInvalidated);
      window.removeEventListener("storage", handleStorage);
    };
  }, [expireSession, navigate]);

  return (
    <AuthContext.Provider value={{ token, role, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
