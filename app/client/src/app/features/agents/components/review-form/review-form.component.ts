import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentService } from '../../../../core/services/agent.service';
import { ReviewCreateRequest } from '../../../../core/models/review.model';

@Component({
    selector: 'app-review-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './review-form.component.html',
    styleUrls: ['./review-form.component.css']
})
export class ReviewFormComponent {
    @Input() agentId!: number;
    @Output() reviewPosted = new EventEmitter<void>();

    score = 5;
    comment = '';
    isSubmitting = false;
    error = '';

    constructor(private agentService: AgentService) { }

    setScore(star: number) {
        this.score = star;
    }

    submitReview() {
        if (!this.comment.trim()) {
            this.error = 'Please write a comment.';
            return;
        }

        this.isSubmitting = true;
        this.error = '';

        const request: ReviewCreateRequest = {
            agentId: this.agentId,
            score: this.score,
            comment: this.comment
        };

        this.agentService.createReview(request).subscribe({
            next: () => {
                this.isSubmitting = false;
                this.comment = '';
                this.score = 5;
                this.reviewPosted.emit();
            },
            error: (err) => {
                console.error(err);
                this.isSubmitting = false;
                this.error = 'Failed to submit review. Please try again.';
            }
        });
    }
}
