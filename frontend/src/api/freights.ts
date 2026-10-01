import api from "./axios";
import type { Freight, FreightRequest, FreightStatus } from "../types/freight";
import type { PagedResponse } from "../types/vehicle";

export interface FreightFilters {
  status?: FreightStatus | "";
  driverName?: string;
  vehicleId?: string; 
  startDate?: string;
  endDate?: string;
  size?: number;
}

export async function listFreights(
  page: number = 0,
  filters?: FreightFilters
): Promise<PagedResponse<Freight>> {
  
  const params: Record<string, any> = {
    page,
    size: 20,
    sort: "createdAt,desc", 
    ...filters,
  };

  if (params.startDate && params.startDate.length === 16) {
    params.startDate = `${params.startDate}:00`;
  }
  if (params.endDate && params.endDate.length === 16) {
    params.endDate = `${params.endDate}:00`;
  }

  Object.keys(params).forEach(key => {
    if (params[key] === "" || params[key] === undefined) {
      delete params[key];
    }
  });

  const response = await api.get<PagedResponse<Freight>>("/api/freights", {
    params,
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

export async function updateFreight(id: string, data: FreightRequest): Promise<Freight> {
  const response = await api.put<Freight>(`/api/freights/${id}`, data);
  return response.data;
}