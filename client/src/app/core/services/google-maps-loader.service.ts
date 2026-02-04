
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class GoogleMapsLoaderService {
    private scriptLoaded = false;

    load(): Promise<void> {
        if (this.scriptLoaded) {
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = `https://maps.googleapis.com/maps/api/js?key=${environment.googleMapsApiKey}&libraries=visualization,places,geometry`;
            script.async = true;
            script.defer = true;
            script.onload = () => {
                this.scriptLoaded = true;
                resolve();
            };
            script.onerror = (error: any) => reject(error);
            document.body.appendChild(script);
        });
    }
}
