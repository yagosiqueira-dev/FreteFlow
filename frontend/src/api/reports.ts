import api from "./axios";
import type { BiWeeklyReport, VehicleProfitReport } from "../types/report";

export async function getBiWeeklyReport(
  driverId: string,
  startDate: string,
  endDate: string
): Promise<BiWeeklyReport> {
  const response = await api.get<BiWeeklyReport>(
    `/api/reports/driver/${driverId}/bi-weekly`,
    { params: { startDate, endDate } }
  );
  return response.data;
}

export async function getVehicleProfitReport(
  vehicleId: string,
  startDate: string,
  endDate: string
): Promise<VehicleProfitReport> {
  const response = await api.get<VehicleProfitReport>(
    `/api/reports/vehicle/${vehicleId}/profit`,
    { params: { startDate, endDate } }
  );
  return response.data;
}

export async function exportVehicleProfitReport(
  vehicleId: string,
  startDate: string,
  endDate: string
): Promise<void> {
  const response = await api.get(
    `/api/reports/vehicle/${vehicleId}/profit/export`,
    { params: { startDate, endDate }, responseType: "blob" }
  );

  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement("a");
  link.href = url;
  link.download = `relatorio_lucro_${vehicleId}_${startDate}_a_${endDate}.xlsx`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}