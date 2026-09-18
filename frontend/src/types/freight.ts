export type FreightStatus = "PENDING" | "IN_PROGRESS" | "DELIVERED" | "CANCELED";

export interface Freight {
  id: string;
  driverId: string;
  driverName: string;
  vehicleId: string;
  vehiclePlate: string;
  storeNames: string[];
  origin: string;
  destinations: string;
  freightValue: number;
  freightDate: string;
  status: FreightStatus;
}

export interface FreightRequest {
  driverId: string;
  vehicleId: string;
  storeIds: string[];
  freightDate: string;
}