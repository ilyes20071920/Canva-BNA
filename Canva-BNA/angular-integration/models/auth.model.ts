// auth.model.ts
// Angular models matching the Spring Boot DTOs

export interface LoginRequest {
  matricule: number;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;       // 'Bearer'
  matricule: number;
  structure: string;
  role: string;       // 'ROLE_ADMIN' | 'ROLE_PRISE_EN_CHARGE' | 'ROLE_CHEF_DIVISION'
}

export interface UserResponse {
  id: number;
  matricule: number;
  structure: string;
  role: string;
  enabled: boolean;
}

export interface RegisterRequest {
  matricule: number;
  password: string;
  structure: string;
  role: string;
}

export type Role = 'ROLE_ADMIN' | 'ROLE_PRISE_EN_CHARGE' | 'ROLE_CHEF_DIVISION';
