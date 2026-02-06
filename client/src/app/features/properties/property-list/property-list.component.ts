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
            // User entered the name of a Place that was not suggested and
            // pressed the Enter key, or the Place Details request failed.
            // We treat it as a text search for city.
            this.filters.city = this.citySearchInput.nativeElement.value;
            this.onFilterChange();
            return;
        }

        // Check if it's a specific address or a generic region/city
        const isLocality = place.address_components?.some(c => c.types.includes('locality'));

        // If it's a "Locality" (City), we behave as before (text search for city)
        // If it sends coordinates, we might as well use them?
        // Actually, user requested: "Match address... look for all types".
        // Plan says:
        // If City/Locality -> Update filters.city, Clear lat/lng/radius
        // If Address -> Use geometry, Clear filters.city

        if (isLocality) {
            // It is a city, e.g. "Rome"
            // We use the name for text filtering as requested
            // AND we update the map center (lat/lng) so the user sees the city
            // BUT we leave radius undefined so we don't filter by distance (backend uses city name)
            this.filters.city = place.name;
            this.filters.latitude = place.geometry.location.lat();
            this.filters.longitude = place.geometry.location.lng();
            this.filters.radius = undefined;
        } else {
            // It is a specific place/address
            // Use spatial search
            this.filters.city = undefined; // Clear city text filter to allow finding properties near this address regardless of city field
            this.filters.latitude = place.geometry.location.lat();
            this.filters.longitude = place.geometry.location.lng();
            this.filters.radius = 5000; // Default 5km radius for address search, effectively showing "properties in this area"

            // Also update the input text visually
            // (The autocomplete does this automatically usually, but we ensure it matches the filter state logic if needed)
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
        // Backend now handles mixed filters (city OR lat/long) correctly via AND/OR logic?
        // Actually propertyService uses AND.
        // If we set filters.city = undefined, it ignores city.
        // If we set filters.lat/long, it uses spatial.
        // This works perfectly for our "Smart" logic.

        const searchFilters = {
            ...this.filters
        };

        // If we are doing a map search (visual map component), we might want to ignore the list-based lat/long?
        // The original code had:
        // const searchFilters = { ...this.filters, latitude: undefined, longitude: undefined, radius: undefined };
        // This was likely to load ALL properties for the client-side map filtering.
        // If we now want SERVER-SIDE searching for addresses, we should pass them.
        // BUT, we have `allProperties` for the map component.

        // Let's look at `loadProperties` original:
        // // Remove geo-filters from the backend request to ensure we get all properties for the map

        // If we filter by address, we return a small subset.
        // If the user wants to see "properties near Via Roma", they expect a small list.
        // So we SHOULD pass the lat/long to the backend.

        // HOWEVER, `allProperties` is used for the Map Component to show pins.
        // If we only fetch 5 properties, the map will only show 5 pins.
        // Maybe that's desired behavior for a search.

        // If the user does a spatial search via the input, we probably want the list to reflect that.
        // So I will remove the explicit `undefined` overrides if they stem from the search bar.
        // But wait, `this.applyMapFilter()` does client-side filtering on `allProperties`.

        // The previous logic regarding `applyMapFilter` and `allProperties` seems to assume we fetch EVERYTHING (or a paginated page?) and then filter client side for the map widget?
        // No, `searchProperties` returns a Page.
        // `allProperties` seems to assign `data`.
        // If we search with lat/long, `data` will be restricted.

        // Let's stick to passing the filters to the service.
        // The previous code explicitly disabled lat/long/radius for the API call.
        // This was weird. It means `allProperties` contained properties matching text filters but NOT spatial filters.
        // Then `applyMapFilter` filtered them client side?
        // `query.subscribe` -> `loadProperties` -> `searchProperties` -> `allProperties = data` -> `applyMapFilter`

        // If I change this to pass lat/long to backend, the backend will return only nearby properties.
        // `allProperties` will be that subset.
        // `applyMapFilter` will filter again? only if `currentMapFilter` is set (from drawing on the map).

        // So, correct approach:
        // Pass the filters (including lat/long from address search) to the backend.

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
