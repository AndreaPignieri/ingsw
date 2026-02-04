import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Agent, AgentCreateRequest, AgentUpdateRequest } from '../models/agent.model';
import { Review, ReviewCreateRequest } from '../models/review.model';

@Injectable({
    providedIn: 'root'
})
export class AgentService {
    private apiUrl = '/api/agents';
    private reviewUrl = '/api/reviews';

    constructor(private http: HttpClient) { }

    createAgent(data: AgentCreateRequest): Observable<string> {
        return this.http.post(`${this.apiUrl}`, data, { responseType: 'text' });
    }

    getAgents(): Observable<Agent[]> {
        return this.http.get<Agent[]>(`${this.apiUrl}`);
    }

    getMyAgents(): Observable<Agent[]> {
        return this.http.get<Agent[]>(`${this.apiUrl}/my-agency`);
    }

    getAgent(id: number): Observable<Agent> {
        return this.http.get<Agent>(`${this.apiUrl}/${id}`);
    }

    updateAgent(id: number, data: AgentUpdateRequest): Observable<string> {
        return this.http.put(`${this.apiUrl}/${id}`, data, { responseType: 'text' });
    }

    createReview(data: ReviewCreateRequest): Observable<Review> {
        return this.http.post<Review>(`${this.reviewUrl}`, data);
    }

    getReviews(agentId: number): Observable<Review[]> {
        return this.http.get<Review[]>(`${this.reviewUrl}/agent/${agentId}`);
    }
}
