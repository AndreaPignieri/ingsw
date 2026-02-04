export enum PropertyType {
  SALE = 'SALE',
  RENT = 'RENT'
}

export interface Property {
  id: number;
  title: string;
  description?: string;
  price: number;
  type: PropertyType;
  city: string;
  rooms?: number;
  floor?: number;
  elevator?: boolean; // Keep for backward compatibility or view logic if needed, though treated as amenity now
  energyClass?: string;
  sizeSqm?: number;
  address?: string;
  bathrooms?: number;
  condition?: string;
  yearBuilt?: number;
  latitude?: number;
  longitude?: number;
  amenities?: string[];
  photos?: string[];

  // Agent/Agency Details
  agentId?: number;
  agentName?: string;
  agentEmail?: string;
  agentPhone?: string;
  agencyName?: string;
}

export interface PropertyCreateRequest {
  title: string;
  description?: string;
  price: number;
  type: PropertyType;
  city: string;
  rooms?: number;
  floor?: number;
  elevator?: boolean;
  energyClass?: string;
  sizeSqm?: number;
  address?: string;
  bathrooms?: number;
  condition?: string;
  yearBuilt?: number;
  latitude?: number;
  longitude?: number;
  amenities?: string[];
  photos?: string[];
}

export interface PropertyUpdateRequest {
  title?: string;
  description?: string;
  price?: number;
  type?: PropertyType;
  city?: string;
  rooms?: number;
  floor?: number;
  sizeSqm?: number;
  address?: string;
  bathrooms?: number;
  condition?: string;
  yearBuilt?: number;
  elevator?: boolean;
  energyClass?: string;
  latitude?: number;
  longitude?: number;
  amenities?: string[];
  photos?: string[];
}

export interface PropertySearchFilters {
  city?: string;
  minPrice?: number;
  maxPrice?: number;
  rooms?: number;
  type?: PropertyType;
  minSize?: number;
  maxSize?: number;
  floor?: number;
  bathrooms?: number;
  energyClass?: string;
  condition?: string;
  latitude?: number;
  longitude?: number;
  radius?: number;
  page?: number;
  limit?: number;
  agentEmail?: string;
}
