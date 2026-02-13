import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class FileUploadService {
    private apiUrl = '/api/uploads';

    constructor(private http: HttpClient) { }

    uploadFile(file: File): Observable<string> {
        const formData = new FormData();
        formData.append('file', file);

        return this.http.post<{ url: string }>(this.apiUrl, formData).pipe(
            map(response => response.url)
        );
    }
}
