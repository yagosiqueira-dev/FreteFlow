export type UserRole = "ADMIN" | "OPERATOR";

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  enabled: boolean;
}

export interface UserRequest {
  name: string;
  email: string;
}