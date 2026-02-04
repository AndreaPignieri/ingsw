import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PropertyService } from '../../core/services/property.service';
import { Property } from '../../core/models/property.model';
import { PropertyCardComponent } from '../../shared/components/property-card/property-card.component';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [CommonModule, PropertyCardComponent, FormsModule],
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
    featuredProperties: Property[] = [];

    // Search filters
    searchCity = '';
    searchType = '';

    constructor(
        private propertyService: PropertyService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.loadFeaturedProperties();
    }

    loadFeaturedProperties() {
        this.propertyService.searchProperties({ limit: 6 }).subscribe({
            next: (data: any) => {
                if (Array.isArray(data)) {
                    this.featuredProperties = data;
                } else if (data && data.content) {
                    this.featuredProperties = data.content;
                }
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error('Error loading featured properties', err);
                this.cdr.detectChanges();
            }
        });
    }

    onSearch() {
        const queryParams: any = {};
        if (this.searchCity) queryParams.city = this.searchCity;
        if (this.searchType) queryParams.type = this.searchType;

        this.router.navigate(['/properties'], { queryParams });
    }
}
