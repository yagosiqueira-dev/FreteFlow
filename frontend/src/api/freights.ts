import api from "./axios";
import type { Freight, FreightRequest, FreightStatus } from "../types/freight";
import type { PagedResponse } from "../types/vehicle";

export async function listFreights(page: number = 0): Promise<PagedResponse<Freight>> {
  const response = await api.get<PagedResponse<Freight>>("/api/freights", {
    params: { page },
  });
  return response.data;
}

export async function createFreight(data: FreightRequest): Promise<Freight> {
  const response = await api.post<Freight>("/api/freights", data);
  return response.data;
}

export async function updateFreightStatus(id: string, status: FreightStatus): Promise<Freight> {
  const response = await api.patch<Freight>(`/api/freights/${id}/status`, null, {
    params: { status },
  });
  return response.data;
}