export interface Store {
  id: string;
  name: string;
  origin: string;
  destination: string;
  defaultValue: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StoreRequest {
  name: string;
  origin: string;
  destination: string;
  defaultValue: number;
}