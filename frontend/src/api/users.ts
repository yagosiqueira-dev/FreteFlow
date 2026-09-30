import api from "./axios";
import type { User, UserRequest } from "../types/user";
import type { PagedResponse } from "../types/vehicle";

export async function listUsers(page: number = 0): Promise<PagedResponse<User>> {
  const response = await api.get<PagedResponse<User>>("/api/users", {
    params: { page },
  });
  return response.data;
}

export async function updateUser(id: string, data: UserRequest): Promise<User> {
  const response = await api.put<User>(`/api/users/${id}`, data);
  return response.data;
}

export async function deactivateUser(id: string): Promise<void> {
  await api.delete(`/api/users/${id}`);
}

export async function activateUser(id: string): Promise<User> {
  const response = await api.patch<User>(`/api/users/${id}/activate`);
  return response.data;
}

export async function promoteUser(id: string): Promise<User> {
  const response = await api.patch<User>(`/api/users/${id}/promote`);
  return response.data;
}

export async function demoteUser(id: string): Promise<User> {
  const response = await api.patch<User>(`/api/users/${id}/demote`);
  return response.data;
}