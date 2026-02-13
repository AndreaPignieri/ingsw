import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = '/api/auth';

    constructor(private http: HttpClient) { }

    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap(response => {
                localStorage.setItem('token', response.token);
                localStorage.setItem('user', JSON.stringify(response.user));
            })
        );
    }

    // Note: register return type might need checking, assuming void for now
    register(data: RegisterRequest): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/register`, data);
    }

    registerAgency(data: any): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/register/agency`, data);
    }

    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    getUser(): any | null {
        try {
            const userStr = localStorage.getItem('user');
            return userStr ? JSON.parse(userStr) : null;
        } catch (e) {
            console.error('Error parsing user from local storage', e);
            return null;
        }
    }

    isAgent(): boolean {
        const user = this.getUser();
        return user?.role === 'AGENT';
    }

    isAgency(): boolean {
        const user = this.getUser();
        return user?.role === 'AGENCY';
    }
}
