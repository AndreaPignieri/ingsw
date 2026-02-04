import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PropertyService } from '../../../core/services/property.service';
import { Property, PropertySearchFilters } from '../../../core/models/property.model';
import { PropertyCardComponent } from '../../../shared/components/property-card/property-card.component';
import { DualRangeSliderComponent } from '../../../shared/components/dual-range-slider/dual-range-slider.component';
import { MapSearchComponent } from '../../../shared/components/map-search/map-search.component';

@Component({
    selector: 'app-property-list',
    standalone: true,
    imports: [CommonModule, FormsModule, PropertyCardComponent, DualRangeSliderComponent, MapSearchComponent],
    templateUrl: './property-list.component.html',
    styleUrls: ['./property-list.component.css']
})
export class PropertyListComponent implements OnInit {
    @ViewChild(MapSearchComponent) mapSearchComponent!: MapSearchComponent;
    properties: Property[] = [];
    allProperties: Property[] = [];
    currentMapFilter: { lat: number, lng: number, radius: number } | null = null;
    filters: PropertySearchFilters = {
        page: 0,
        limit: 12,
        radius: 5000 // Default radius 5km
    };
    isLoading = false;

    constructor(
        private propertyService: PropertyService,
        private route: ActivatedRoute,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            this.filters = {
                ...this.filters,
                city: params['city'] || '',
                type: params['type'],
                minPrice: params['minPrice'] ? Number(params['minPrice']) : undefined,
                maxPrice: params['maxPrice'] ? Number(params['maxPrice']) : undefined,
                rooms: params['rooms'] ? Number(params['rooms']) : undefined,
                minSize: params['minSize'] ? Number(params['minSize']) : undefined,
                maxSize: params['maxSize'] ? Number(params['maxSize']) : undefined,
                floor: params['floor'] ? Number(params['floor']) : undefined,
                bathrooms: params['bathrooms'] ? Number(params['bathrooms']) : undefined,
                energyClass: params['energyClass'],
                condition: params['condition'],
                latitude: params['latitude'] ? Number(params['latitude']) : undefined,
                longitude: params['longitude'] ? Number(params['longitude']) : undefined,
                radius: params['radius'] ? Number(params['radius']) : 5000
            };

            this.loadProperties();
        });
    }



    incrementRooms() {
        this.filters.rooms = (this.filters.rooms || 0) + 1;
        this.onFilterChange();
    }

    decrementRooms() {
        if (this.filters.rooms && this.filters.rooms > 0) {
            this.filters.rooms--;
            if (this.filters.rooms === 0) this.filters.rooms = undefined;
            this.onFilterChange();
        }
    }

    incrementBathrooms() {
        this.filters.bathrooms = (this.filters.bathrooms || 0) + 1;
        this.onFilterChange();
    }

    decrementBathrooms() {
        if (this.filters.bathrooms && this.filters.bathrooms > 0) {
            this.filters.bathrooms--;
            if (this.filters.bathrooms === 0) this.filters.bathrooms = undefined;
            this.onFilterChange();
        }
    }

    clearFilters() {
        this.filters = {
            page: 0,
            limit: 12
        };

        // Clear map selection
        this.currentMapFilter = null;
        if (this.mapSearchComponent) {
            this.mapSearchComponent.clearSelection();
        }
        this.applyMapFilter();

        // Use default navigation (replace) to wipe existing params
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: this.filters
        });
    }

    formatPrice(value: number): string {
        if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
        if (value >= 1000) return (value / 1000).toFixed(0) + 'k';
        return value.toString();
    }

    formatSize(value: number): string {
        return value.toString();
    }

    loadProperties() {
        this.isLoading = true;
        // Remove geo-filters from the backend request to ensure we get all properties for the map
        const searchFilters = {
            ...this.filters,
            latitude: undefined,
            longitude: undefined,
            radius: undefined
        };

        this.propertyService.searchProperties(searchFilters).subscribe({
            next: (data: Property[]) => {
                this.allProperties = data;
                this.applyMapFilter();
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error(err);
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onMapSearch(event: { lat: number, lng: number, radius: number }) {
        this.currentMapFilter = event;
        this.applyMapFilter();
    }

    applyMapFilter() {
        if (this.currentMapFilter) {
            const { lat, lng, radius } = this.currentMapFilter;
            this.properties = this.allProperties.filter(p => {
                if (!p.latitude || !p.longitude) return false;
                return this.calculateDistance(lat, lng, Number(p.latitude), Number(p.longitude)) <= radius;
            });
        } else {
            this.properties = [...this.allProperties];
        }
    }

    onFilterChange() {
        // Create a params object that allows nulls
        const queryParams: any = { ...this.filters };
        delete queryParams.page; // Reset page on filter change
        delete queryParams.limit;

        // For merge strategy, we must explicitly set removed keys to null
        // instead of deleting them, otherwise the old value persists.
        Object.keys(queryParams).forEach(key => {
            if (queryParams[key] === undefined || queryParams[key] === '') {
                queryParams[key] = null;
            }
        });

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: queryParams,
            queryParamsHandling: 'merge'
        });
    }

    private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
        const R = 6371e3; // metres
        const φ1 = lat1 * Math.PI / 180;
        const φ2 = lat2 * Math.PI / 180;
        const Δφ = (lat2 - lat1) * Math.PI / 180;
        const Δλ = (lon2 - lon1) * Math.PI / 180;

        const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
