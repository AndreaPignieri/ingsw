import { Property } from './property.model';

export interface Agent {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  biography?: string;
  profilePhoto?: string;
  birthDate?: string;
  phoneNumber?: string;
  agencyName?: string;
  properties?: Property[];
}

export interface AgentCreateRequest {
  firstName: string;
  lastName: string;
  biography?: string;
  profilePhoto?: string;
}

export interface AgentUpdateRequest {
  firstName?: string;
  lastName?: string;
  biography?: string;
  profilePhoto?: string;
}
