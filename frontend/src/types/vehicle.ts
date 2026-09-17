export interface Vehicle {
  id: string;
  licensePlate: string;
  type: string;
  model: string;
  year: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface VehicleRequest {
  licensePlate: string;
  type: string;
  model: string;
  year: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}