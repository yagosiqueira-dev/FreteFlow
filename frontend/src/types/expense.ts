export interface Expense {
  id: string;
  vehicleId: string;
  description: string;
  amount: number;
  expenseDate: string;
  enabled: boolean;
  createdAt: string;
}

export interface ExpenseRequest {
  vehicleId: string;
  description: string; 
  amount: number;
  expenseDate: string;
}