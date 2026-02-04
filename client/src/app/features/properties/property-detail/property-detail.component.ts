import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PropertyService } from '../../../core/services/property.service';
import { Property } from '../../../core/models/property.model';

@Component({
    selector: 'app-property-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './property-detail.component.html',
    styleUrls: ['./property-detail.component.css']
})
export class PropertyDetailComponent implements OnInit {
    property: Property | null = null;
    isLoading = true;
    error = '';
    activeImageIndex = 0;

    constructor(
        private route: ActivatedRoute,
        private propertyService: PropertyService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadProperty(Number(id));
        } else {
            this.error = 'Property ID not found';
            this.isLoading = false;
        }
    }

    loadProperty(id: number) {
        this.propertyService.getProperty(id).subscribe({
            next: (data: Property) => {
                this.property = data;
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error(err);
                this.error = 'Failed to load property details';
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    setActiveImage(index: number) {
        this.activeImageIndex = index;
    }

    nextImage() {
        if (!this.property?.photos) return;
        this.activeImageIndex = (this.activeImageIndex + 1) % this.property.photos.length;
    }

    prevImage() {
        if (!this.property?.photos) return;
        this.activeImageIndex = (this.activeImageIndex - 1 + this.property.photos.length) % this.property.photos.length;
    }
}
