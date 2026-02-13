import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AgentService } from '../../../core/services/agent.service';
import { AuthService } from '../../../core/services/auth.service';
import { Agent } from '../../../core/models/agent.model';
import { Review } from '../../../core/models/review.model';
import { PropertyCardComponent } from '../../../shared/components/property-card/property-card.component';
import { ReviewListComponent } from '../components/review-list/review-list.component';
import { ReviewFormComponent } from '../components/review-form/review-form.component';

@Component({
    selector: 'app-agent-detail',
    standalone: true,
    imports: [CommonModule, PropertyCardComponent, ReviewListComponent, ReviewFormComponent],
    templateUrl: './agent-detail.component.html',
    styleUrls: ['./agent-detail.component.css']
})
export class AgentDetailComponent implements OnInit {
    agent: Agent | null = null;
    reviews: Review[] = [];
    isLoading = true;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private agentService: AgentService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef
    ) { }

    get canPostReview(): boolean {
        const user = this.authService.getUser();
        // Only allow logged in users with ROLE='USER' (Clients) to post reviews.
        // Agents and Agencies cannot post reviews.
        return !!user && user.role === 'USER';
    }

    get isLoggedIn(): boolean {
        return !!this.authService.getUser();
    }

    showReviewModal = false;
    averageRating = 0;

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadAgent(Number(id));
        } else {
            this.error = 'Agent ID not found';
            this.isLoading = false;
        }
    }

    loadAgent(id: number) {
        this.agentService.getAgent(id).subscribe({
            next: (data: Agent) => {
                console.log('Agent loaded:', data);
                this.agent = data;
                this.isLoading = false;
                this.cdr.detectChanges(); // Force view update
                this.loadReviews(id);
            },
            error: (err: any) => {
                console.error(err);
                this.error = 'Failed to load agent profile';
                this.isLoading = false;
            }
        });
    }

    loadReviews(id: number) {
        this.agentService.getReviews(id)
            .subscribe({
                next: (data: Review[]) => {
                    this.reviews = data;
                    this.calculateAverageRating();
                    this.cdr.detectChanges();
                },
                error: (err: any) => {
                    console.error('Failed to load reviews', err);
                }
            });
    }

    calculateAverageRating() {
        if (!this.reviews || this.reviews.length === 0) {
            this.averageRating = 0;
            return;
        }
        const total = this.reviews.reduce((sum, review) => sum + review.score, 0);
        this.averageRating = total / this.reviews.length;
    }

    openReviewModal() {
        this.showReviewModal = true;
    }

    closeReviewModal() {
        this.showReviewModal = false;
    }

    onReviewPosted() {
        this.closeReviewModal();
        if (this.agent) {
            this.loadReviews(this.agent.id);
        }
    }

    getAge(birthDate: any): number | string {
        if (!birthDate) return 'N/A';
        // Handle array format [yyyy, mm, dd] if it comes that way
        let birth: Date;
        if (Array.isArray(birthDate)) {
            birth = new Date(birthDate[0], birthDate[1] - 1, birthDate[2]);
        } else {
            birth = new Date(birthDate);
        }

        if (isNaN(birth.getTime())) return 'N/A';

        const today = new Date();
        let age = today.getFullYear() - birth.getFullYear();
        const m = today.getMonth() - birth.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
            age--;
        }
        return age;
    }
}
