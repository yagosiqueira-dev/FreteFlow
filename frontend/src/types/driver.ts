export interface Driver {
  id: string;
  name: string;
  phone: string;
  cpf: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DriverRequest {
  name: string;
  phone: string;
  cpf: string;
}