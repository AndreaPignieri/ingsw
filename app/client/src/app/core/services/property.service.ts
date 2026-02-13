import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap, timeout } from 'rxjs';
import { Property, PropertyCreateRequest, PropertyUpdateRequest, PropertySearchFilters } from '../models/property.model';

@Injectable({
    providedIn: 'root'
})
export class PropertyService {
    private apiUrl = '/api/properties';

    constructor(private http: HttpClient) { }

    searchProperties(filters: PropertySearchFilters): Observable<Property[]> {
        let params = new HttpParams();
        Object.keys(filters).forEach(key => {
            const value = (filters as any)[key];
            if (value !== undefined && value !== null && value !== '') {
                params = params.set(key, value.toString());
            }
        });

        return this.http.get<any>(this.apiUrl, { params }).pipe(
            timeout(5000),
            tap((response: any) => console.log('API Response:', response)),
            map((response: any) => response.content as Property[])
        );
    }

    createProperty(data: PropertyCreateRequest): Observable<Property> {
        return this.http.post<Property>(this.apiUrl, data);
    }

    getProperty(id: number): Observable<Property> {
        return this.http.get<Property>(`${this.apiUrl}/${id}`);
    }

    updateProperty(id: number, data: PropertyUpdateRequest): Observable<string> {
        return this.http.put(`${this.apiUrl}/${id}`, data, { responseType: 'text' });
    }
}
