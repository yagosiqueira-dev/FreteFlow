import api from "./axios";
import type { Store, StoreRequest } from "../types/store";
import type { PagedResponse } from "../types/vehicle";

export async function listStores(page: number = 0): Promise<PagedResponse<Store>> {
  const response = await api.get<PagedResponse<Store>>("/api/stores", {
    params: { page },
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