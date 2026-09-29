export interface FreightReportItem {
  date: string;
  loadingLocation: string;
  fullRoute: string;
  value: number;
}

export interface BiWeeklyReport {
  startDate: string;
  endDate: string;
  driverName: string;
  totalAmount: number;
  freights: FreightReportItem[];
}

export interface FreightSummary {
  date: string;
  driverName: string;
  storeName: string;
  value: number;
}

export interface ExpenseSummary {
  date: string;
  description: string;
  amount: number;
}

export interface VehicleProfitReport {
  startDate: string;
  endDate: string;
  vehicleId: string;
  licensePlate: string;
  totalFreightValue: number;
  totalExpenses: number;
  netProfit: number;
  freights: FreightSummary[];
  expenses: ExpenseSummary[];
}