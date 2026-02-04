import { Component, Input, Output, EventEmitter, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { GoogleMapsModule, MapInfoWindow, MapMarker, MapCircle } from '@angular/google-maps';
import { Property } from '../../../core/models/property.model';

@Component({
    selector: 'app-map-search',
    standalone: true,
    imports: [CommonModule, GoogleMapsModule],
    templateUrl: './map-search.component.html',
    styleUrls: ['./map-search.component.css']
})
export class MapSearchComponent implements OnChanges {
    @Input() properties: Property[] = [];
    @Input() center: { lat: number, lng: number } = { lat: 41.9028, lng: 12.4964 }; // Rome default
    @Input() radius: number = 5000; // meters
    @Input() mode: 'search' | 'pick' = 'search';
    @Input() set pickedLocation(value: { lat: number, lng: number } | null) {
        if (value) {
            this.pickedMarker = {
                position: value,
                title: 'Selected Location'
            };
            this.center = value;
        }
    }
    @Output() searchAreaChange = new EventEmitter<{ lat: number, lng: number, radius: number }>();
    @Output() locationPick = new EventEmitter<{ lat: number, lng: number }>();

    @ViewChild(MapInfoWindow) infoWindow: MapInfoWindow | undefined;
    @ViewChild(MapCircle) mapCircle: MapCircle | undefined;

    zoom = 12;
    mapOptions: google.maps.MapOptions = {
        disableDefaultUI: false,
        zoomControl: true,
        scrollwheel: true,
    };

    circleOptions: google.maps.CircleOptions = {
        fillColor: '#4285F4',
        fillOpacity: 0.1,
        strokeColor: '#4285F4',
        strokeOpacity: 0.8,
        strokeWeight: 1,
        draggable: true,
        editable: true
    };

    markers: any[] = [];
    pickedMarker: any = null;
    selectedProperty: Property | null = null;
    isSearchActive = false;
    private debounceTimer: any;

    constructor(private router: Router) { }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['properties']) {
            this.updateMarkers();
        }
    }

    updateMarkers() {
        this.markers = this.properties
            .filter(p => p.latitude && p.longitude)
            .map(p => ({
                position: { lat: Number(p.latitude), lng: Number(p.longitude) },
                title: p.title,
                data: p
            }));
    }

    openInfoWindow(marker: MapMarker, property: Property) {
        this.selectedProperty = property;
        this.infoWindow?.open(marker);
    }

    onMapClick(event: google.maps.MapMouseEvent) {
        if (event.latLng) {
            const lat = event.latLng.lat();
            const lng = event.latLng.lng();

            if (this.mode === 'search') {
                this.isSearchActive = true;
                this.center = { lat, lng };
                this.emitChange();
            } else {
                // Pick mode
                this.pickedMarker = {
                    position: { lat, lng },
                    title: 'Selected Location'
                };
                this.locationPick.emit({ lat, lng });
            }
        }
    }

    onCircleEvent() {
        // Debounce the emission to prevent rapid API calls while dragging/resizing
        if (this.debounceTimer) clearTimeout(this.debounceTimer);

        this.debounceTimer = setTimeout(() => {
            if (this.mapCircle) {
                // Accessing underlying google.maps.Circle. 
                // Using 'any' cast to avoid TS issues if type definition is mismatching
                const circleInstance = (this.mapCircle as any).getCircle ? (this.mapCircle as any).getCircle() : (this.mapCircle as any).circle;

                if (circleInstance) {
                    const newRadius = circleInstance.getRadius();
                    const newCenter = circleInstance.getCenter();

                    if (newCenter) {
                        this.center = { lat: newCenter.lat(), lng: newCenter.lng() };
                    }
                    if (newRadius) {
                        this.radius = newRadius;
                    }

                    this.emitChange();
                }
            }
        }, 500); // 500ms delay
    }

    emitChange() {
        this.searchAreaChange.emit({
            lat: this.center.lat,
            lng: this.center.lng,
            radius: this.radius
        });
    }

    goToDetails(property: Property) {
        if (property && property.id) {
            this.router.navigate(['/properties', property.id]);
        }
    }

    clearSelection() {
        this.isSearchActive = false;
        // Optional: Reset to default center if needed, but keeping current view is usually better UX
        // this.center = { ...default }; 
    }
}
