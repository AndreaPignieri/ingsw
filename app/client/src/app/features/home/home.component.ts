import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, NgZone, AfterViewInit, OnDestroy } from '@angular/core';
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
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('citySearch') citySearchInput!: ElementRef;
    featuredProperties: Property[] = [];

    // Search filters
    searchCity = '';
    searchType = '';

    private autocomplete: google.maps.places.Autocomplete | undefined;
    private placeListener: google.maps.MapsEventListener | undefined;
    private selectedLocation: { lat: number, lng: number, isLocality: boolean } | null = null;

    constructor(
        private propertyService: PropertyService,
        private router: Router,
        private cdr: ChangeDetectorRef,
        private ngZone: NgZone
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
                types: [], // Allow all
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
            this.searchCity = this.citySearchInput.nativeElement.value;
            this.selectedLocation = null;
            return;
        }

        const isLocality = place.address_components?.some(c => c.types.includes('locality'));

        this.selectedLocation = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng(),
            isLocality: !!isLocality
        };

        if (isLocality) {
            this.searchCity = place.name || '';
        } else {
            this.searchCity = this.citySearchInput.nativeElement.value;
        }
    }

    onSearch() {
        const queryParams: any = {};

        if (this.searchType) queryParams.type = this.searchType;

        if (this.selectedLocation) {
            if (this.selectedLocation.isLocality) {
                queryParams.city = this.searchCity;
                queryParams.latitude = this.selectedLocation.lat;
                queryParams.longitude = this.selectedLocation.lng;
            } else {
                queryParams.latitude = this.selectedLocation.lat;
                queryParams.longitude = this.selectedLocation.lng;
                queryParams.radius = 5000;
            }
        } else {
            if (this.searchCity) queryParams.city = this.searchCity;
        }

        this.router.navigate(['/properties'], { queryParams });
    }
}
