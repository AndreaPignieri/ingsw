import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserUpdateRequest } from '../models/auth.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = '/api/users';

    constructor(private http: HttpClient) { }

    getProfile(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`);
    }

    updateProfile(data: UserUpdateRequest): Observable<string> {
        return this.http.put(`${this.apiUrl}/me`, data, { responseType: 'text' });
    }
}
