import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Agency {
    id: number;
    name: string;
    address: string;
    phone: string;
    email: string;
}

export interface AgencyUpdateRequest {
    name?: string;
    address?: string;
    phone?: string;
    email?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AgencyService {
    private apiUrl = '/api/agencies';

    constructor(private http: HttpClient) { }

    getMyAgency(): Observable<Agency> {
        return this.http.get<Agency>(`${this.apiUrl}/me`);
    }

    updateMyAgency(data: AgencyUpdateRequest): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/me`, data);
    }
}
