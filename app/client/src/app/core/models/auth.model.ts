export interface LoginRequest {
  email: string;
  password: string;
  role?: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface User {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  biography?: string;
  profilePhoto?: string;
  authProvider?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  password?: string;
  oldPassword?: string;
  biography?: string;
  profilePhoto?: string;
}
