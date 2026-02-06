import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, NgZone, AfterViewInit, OnDestroy } from '@angular/core';
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
export class PropertyListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MapSearchComponent) mapSearchComponent!: MapSearchComponent;
    @ViewChild('citySearch') citySearchInput!: ElementRef;

    properties: Property[] = [];
    allProperties: Property[] = [];
    currentMapFilter: { lat: number, lng: number, radius: number } | null = null;

    private autocomplete: google.maps.places.Autocomplete | undefined;
    private placeListener: google.maps.MapsEventListener | undefined;

    filters: PropertySearchFilters = {
        page: 0,
        limit: 12,
        radius: undefined // Default is undefined (global search unless address specified)
    };
    isLoading = false;

    constructor(
        private propertyService: PropertyService,
        private route: ActivatedRoute,
        private router: Router,
        private cdr: ChangeDetectorRef,
        private ngZone: NgZone
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
                radius: params['radius'] ? Number(params['radius']) : undefined
            };

            this.loadProperties();
        });
    }



    ngAfterViewInit() {
        this.initAutocomplete();
    }

    ngOnDestroy() {
        if (this.placeListener) {
            this.placeListener.remove();
        }
    }

    initAutocomplete() {
        if (!this.citySearchInput) return;

        this.autocomplete = new google.maps.places.Autocomplete(
            this.citySearchInput.nativeElement,
            {
                types: [], // Allow all types (cities, addresses, businesses)
                componentRestrictions: { country: 'it' },
                fields: ['address_components', 'geometry', 'name']
            }
        );

        this.placeListener = this.autocomplete.addListener('place_changed', () => {
            this.ngZone.run(() => {
                const place = this.autocomplete?.getPlace();
                if (place) {
                    this.handlePlaceSelect(place);
                }
            });
        });
    }

    handlePlaceSelect(place: google.maps.places.PlaceResult) {
        if (!place.geometry || !place.geometry.location) {
            this.filters.city = this.citySearchInput.nativeElement.value;
            this.onFilterChange();
            return;
        }

        const isLocality = place.address_components?.some(c => c.types.includes('locality'));

        if (isLocality) {
            this.filters.city = place.name;
            this.filters.latitude = place.geometry.location.lat();
            this.filters.longitude = place.geometry.location.lng();
            this.filters.radius = undefined;
        } else {
            this.filters.city = undefined;
            this.filters.latitude = place.geometry.location.lat();
            this.filters.longitude = place.geometry.location.lng();
            this.filters.radius = 5000;
        }

        this.onFilterChange();
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

        // Clear input manually if needed
        if (this.citySearchInput) {
            this.citySearchInput.nativeElement.value = '';
        }

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
        const searchFilters = {
            ...this.filters
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
