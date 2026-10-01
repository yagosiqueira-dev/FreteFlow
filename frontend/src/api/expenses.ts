import api from "./axios";
import type { Expense, ExpenseRequest } from "../types/expense";
import type { PagedResponse } from "../types/vehicle";

export interface ExpenseFilters {
  vehicleId?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  size?: number;
}

export async function listExpenses(page: number = 0, filters: ExpenseFilters = {}): Promise<PagedResponse<Expense>> {
  const response = await api.get<PagedResponse<Expense>>("/api/expenses", {
    params: { page, size: 20, ...filters },
  });
  return response.data;
}

export async function createExpense(data: ExpenseRequest): Promise<Expense> {
  const response = await api.post<Expense>("/api/expenses", data);
  return response.data;
}

export async function updateExpense(id: string, data: ExpenseRequest): Promise<Expense> {
  const response = await api.put<Expense>(`/api/expenses/${id}`, data);
  return response.data;
}

export async function deactivateExpense(id: string): Promise<void> {
  await api.delete(`/api/expenses/${id}`);
}

export async function activateExpense(id: string): Promise<Expense> {
  const response = await api.patch<Expense>(`/api/expenses/${id}/activate`);
  return response.data;
}