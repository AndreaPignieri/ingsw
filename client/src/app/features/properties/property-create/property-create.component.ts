import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit, NgZone, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { PropertyService } from '../../../core/services/property.service';
import { PropertyType } from '../../../core/models/property.model';
import { MapSearchComponent } from '../../../shared/components/map-search/map-search.component';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ImageResizerService } from '../../../core/services/image-resizer.service';

@Component({
    selector: 'app-property-create',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MapSearchComponent],
    templateUrl: './property-create.component.html',
    styleUrls: ['./property-create.component.css']
})
export class PropertyCreateComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('addressSearch') addressSearchInput!: ElementRef;

    propertyForm: FormGroup;
    private autocomplete: google.maps.places.Autocomplete | undefined;
    private placeListener: google.maps.MapsEventListener | undefined;
    private geocoder: google.maps.Geocoder | undefined;

    propertyTypes = Object.values(PropertyType);
    isLoading = false;
    error = '';
    uploadingPhotos = false;

    // UI States
    mapLocation: { lat: number, lng: number } | null = null;
    showManualAddress = false;
    displayPrice = ''; // For visual formatting

    // Hardcoded common amenities for selection
    availableAmenities = ['Elevator', 'Air Conditioning', 'Concierge', 'Garden', 'Balcony', 'Garage', 'Pool', 'Heating'];

    constructor(
        private fb: FormBuilder,
        private propertyService: PropertyService,
        private router: Router,
        private cdr: ChangeDetectorRef,
        private ngZone: NgZone,
        private fileUploadService: FileUploadService,
        private imageResizer: ImageResizerService
    ) {
        this.propertyForm = this.fb.group({
            title: ['', Validators.required],
            description: [''],
            price: [null, [Validators.required, Validators.min(0)]],
            type: [PropertyType.SALE, Validators.required],
            city: ['', Validators.required],
            address: ['', Validators.required],
            rooms: [null, [Validators.required, Validators.min(0)]],
            floor: [null, Validators.required],
            bathrooms: [null, [Validators.required, Validators.min(0)]],
            sizeSqm: [null, [Validators.required, Validators.min(0)]],
            condition: ['', Validators.required],
            // yearBuilt removed
            energyClass: ['', Validators.required],
            latitude: [null, Validators.required],
            longitude: [null, Validators.required],
            amenities: this.fb.array([]),
            photoUrls: this.fb.array([]) // Will contain URLs of uploaded files
        });
    }

    ngOnInit(): void { }

    ngAfterViewInit() {
        this.initAutocomplete();
    }

    ngOnDestroy() {
        if (this.placeListener) {
            this.placeListener.remove();
        }
    }

    // ... (Map and Address logic remains unchanged) ...
    initAutocomplete() {
        if (!this.addressSearchInput) return;

        this.geocoder = new google.maps.Geocoder();

        this.autocomplete = new google.maps.places.Autocomplete(
            this.addressSearchInput.nativeElement,
            {
                types: ['address'],
                componentRestrictions: { country: 'it' },
                fields: ['address_components', 'geometry', 'name', 'formatted_address']
            }
        );

        this.placeListener = this.autocomplete.addListener('place_changed', () => {
            this.ngZone.run(() => {
                const place = this.autocomplete?.getPlace();
                if (place) {
                    this.fillInAddress(place);
                }
            });
        });
    }

    fillInAddress(place: google.maps.places.PlaceResult) {
        if (!place.geometry || !place.geometry.location) {
            console.warn('Place result has no geometry');
            return;
        }

        let streetNumber = '';
        let route = '';
        let city = '';

        if (place.address_components) {
            for (const component of place.address_components) {
                const type = component.types[0];
                switch (type) {
                    case 'street_number':
                        streetNumber = component.long_name;
                        break;
                    case 'route':
                        route = component.long_name;
                        break;
                    case 'locality':
                        city = component.long_name;
                        break;
                    case 'administrative_area_level_3':
                        if (!city) city = component.long_name;
                        break;
                }
            }
        }

        let address = `${route} ${streetNumber}`.trim();
        if (!address && place.name) {
            address = place.name;
        }

        this.propertyForm.patchValue({
            city: city,
            address: address,
            latitude: place.geometry.location.lat(),
            longitude: place.geometry.location.lng()
        });

        this.mapLocation = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng()
        };
    }

    onLocationPicked(location: { lat: number, lng: number }) {
        this.mapLocation = location;
        this.propertyForm.patchValue({
            latitude: location.lat,
            longitude: location.lng
        });
        this.geocodeLocation(location);
    }

    private geocodeLocation(location: { lat: number, lng: number }) {
        if (!this.geocoder) return;
        this.geocoder.geocode({ location }, (results, status) => {
            if (status === 'OK' && results && results[0]) {
                this.ngZone.run(() => {
                    const place = results[0];
                    if (this.addressSearchInput) {
                        this.addressSearchInput.nativeElement.value = place.formatted_address;
                    }
                    this.fillInAddress(place);
                });
            } else {
                console.error('Geocoder failed due to: ' + status);
            }
        });
    }
    // ... (End of Map and Address logic) ...

    // Amenities Logic
    onAmenityChange(event: any, amenity: string) {
        const amenities = this.propertyForm.get('amenities') as FormArray;
        if (event.target.checked) {
            amenities.push(this.fb.control(amenity));
        } else {
            const index = amenities.controls.findIndex(x => x.value === amenity);
            amenities.removeAt(index);
        }
    }

    // Price Formatting
    onPriceInput(event: any) {
        let value = event.target.value;
        // Remove non-numeric characters except maybe decimal point if we allowed cents, but lets stick to integers for simplicity or standard float
        // logic: clear non-digits
        // If user types '1.000', valid. 
        // Let's stripping all non-digits
        const numericValue = value.replace(/[^0-9]/g, '');

        if (numericValue) {
            // Update form control with number
            const number = parseInt(numericValue, 10);
            this.propertyForm.get('price')?.setValue(number, { emitEvent: false });

            // Format for display: 1.000.000
            this.displayPrice = number.toLocaleString('it-IT');
            event.target.value = this.displayPrice; // Force updates view
        } else {
            this.propertyForm.get('price')?.setValue(null);
            this.displayPrice = '';
        }
    }

    // Photo Upload Logic
    get photoUrls() {
        return this.propertyForm.get('photoUrls') as FormArray;
    }

    onFileSelected(event: any) {
        const files: FileList = event.target.files;
        if (files && files.length > 0) {
            this.uploadingPhotos = true;
            // Convert FileList to Array to use forEach/reduce
            const fileArray = Array.from(files);

            // Process files sequentially or parallel. Parallel is fine.
            let completed = 0;

            fileArray.forEach(file => {
                // Resize logic similar to Profile
                this.imageResizer.resizeImage(file, 1200, 1200, 0.85).subscribe({
                    next: (resizedFile) => {
                        this.fileUploadService.uploadFile(resizedFile).subscribe({
                            next: (url) => {
                                this.photoUrls.push(this.fb.control(url));
                                completed++;
                                if (completed === fileArray.length) {
                                    this.uploadingPhotos = false;
                                    this.cdr.detectChanges();
                                }
                            },
                            error: (err) => {
                                console.error('Upload failed for file', file.name, err);
                                completed++;
                                if (completed === fileArray.length) {
                                    this.uploadingPhotos = false;
                                    this.cdr.detectChanges();
                                }
                            }
                        });
                    },
                    error: (err) => {
                        console.error('Resize failed for file', file.name, err);
                        completed++;
                        if (completed === fileArray.length) {
                            this.uploadingPhotos = false;
                            this.cdr.detectChanges();
                        }
                    }
                });
            });
        }
    }

    removePhoto(index: number) {
        this.photoUrls.removeAt(index);
    }

    toggleManualAddress() {
        this.showManualAddress = !this.showManualAddress;
    }

    onSubmit() {
        if (this.propertyForm.invalid) {
            this.propertyForm.markAllAsTouched();
            return;
        }

        this.isLoading = true;
        this.error = '';

        const formValue = this.propertyForm.value;
        const requestData = {
            ...formValue,
            photos: formValue.photoUrls // The form array already contains strings
            // yearBuilt is removed from form, so won't be in value
        };

        // Clean up internal fields if any
        delete requestData.photoUrls; // Handled by 'photos' mapping just in case, or backend expects 'photos'

        // Map to backend expectation if needed. DTO likely expects 'photos' as List<String>.
        // Ensure propertyForm.value.photoUrls is string[]
        requestData.photos = this.photoUrls.value;

        this.propertyService.createProperty(requestData).subscribe({
            next: (property) => {
                this.isLoading = false;
                this.router.navigate(['/properties', property.id]);
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.isLoading = false;
                this.error = 'Failed to create property. Please try again.';
                console.error(err);
                this.cdr.detectChanges();
            }
        });
    }
}
