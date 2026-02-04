import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Review } from '../../../../core/models/review.model';

@Component({
    selector: 'app-review-list',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './review-list.component.html',
    styleUrls: ['./review-list.component.css']
})
export class ReviewListComponent {
    @Input() reviews: Review[] = [];
}
