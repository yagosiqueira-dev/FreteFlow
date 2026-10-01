import api from "./axios";
import type { Vehicle, VehicleRequest, PagedResponse } from "../types/vehicle";

export async function listVehicles(page: number = 0): Promise<PagedResponse<Vehicle>> {
  const response = await api.get<PagedResponse<Vehicle>>("/api/vehicles", {
    params: { page, size: 20 },
  });
  return response.data;
}

export async function createVehicle(data: VehicleRequest): Promise<Vehicle> {
  const response = await api.post<Vehicle>("/api/vehicles", data);
  return response.data;
}

export async function updateVehicle(id: string, data: VehicleRequest): Promise<Vehicle> {
  const response = await api.put<Vehicle>(`/api/vehicles/${id}`, data);
  return response.data;
}

export async function deactivateVehicle(id: string): Promise<void> {
  await api.delete(`/api/vehicles/${id}`);
}

export async function activateVehicle(id: string): Promise<Vehicle> {
  const response = await api.patch<Vehicle>(`/api/vehicles/${id}/activate`);
  return response.data;
}