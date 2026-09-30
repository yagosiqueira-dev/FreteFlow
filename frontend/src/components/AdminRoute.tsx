import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import type { ReactNode } from "react";

export function AdminRoute({ children }: { children: ReactNode }) {
  const { role } = useAuth();

  if (role !== "ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}