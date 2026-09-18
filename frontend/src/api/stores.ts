import api from "./axios";
import type { Store, StoreRequest } from "../types/store";
import type { PagedResponse } from "../types/vehicle";

export interface StoreFilters {
  name?: string;
  origin?: string;
  destination?: string;
  enabled?: boolean;
  minValue?: number;
  maxValue?: number;
}

export async function listStores(page: number = 0, filters: StoreFilters = {}, size: number = 20): Promise<PagedResponse<Store>> {
  const response = await api.get<PagedResponse<Store>>("/api/stores", {
    params: { page, size, ...filters },
  });
  return response.data;
}

export async function createStore(data: StoreRequest): Promise<Store> {
  const response = await api.post<Store>("/api/stores", data);
  return response.data;
}

export async function deactivateStore(id: string): Promise<void> {
  await api.delete(`/api/stores/${id}`);
}

export async function activateStore(id: string): Promise<Store> {
  const response = await api.patch<Store>(`/api/stores/${id}/activate`);
  return response.data;
}