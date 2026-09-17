import api from "./axios";
import type { Driver, DriverRequest } from "../types/driver";
import type { PagedResponse } from "../types/vehicle";

export async function listDrivers(): Promise<PagedResponse<Driver>> {
  const response = await api.get<PagedResponse<Driver>>("/api/drivers");
  return response.data;
}

export async function createDriver(data: DriverRequest): Promise<Driver> {
  const response = await api.post<Driver>("/api/drivers", data);
  return response.data;
}

export async function deactivateDriver(id: string): Promise<void> {
  await api.delete(`/api/drivers/${id}`);
}

export async function activateDriver(id: string): Promise<Driver> {
  const response = await api.patch<Driver>(`/api/drivers/${id}/activate`);
  return response.data;
}