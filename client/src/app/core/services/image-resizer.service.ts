import { Injectable, NgZone } from '@angular/core';
import { Observable, from } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ImageResizerService {

    constructor(private zone: NgZone) { }

    /**
     * Resizes an image to a maximum width/height while maintaining aspect ratio.
     * Returns a compressed Blob.
     */
    resizeImage(file: File, maxWidth: number, maxHeight: number, quality: number = 0.8): Observable<File> {
        return from(new Promise<File>((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);

            reader.onload = (event: any) => {
                const img = new Image();
                img.src = event.target.result;

                img.onload = () => {
                    let width = img.width;
                    let height = img.height;

                    // Calculate new dimensions
                    if (width > height) {
                        if (width > maxWidth) {
                            height = Math.round(height * (maxWidth / width));
                            width = maxWidth;
                        }
                    } else {
                        if (height > maxHeight) {
                            width = Math.round(width * (maxHeight / height));
                            height = maxHeight;
                        }
                    }

                    const canvas = document.createElement('canvas');
                    canvas.width = width;
                    canvas.height = height;

                    const ctx = canvas.getContext('2d');
                    if (!ctx) {
                        this.zone.run(() => reject(new Error('Canvas context not available')));
                        return;
                    }

                    ctx.drawImage(img, 0, 0, width, height);

                    canvas.toBlob((blob) => {
                        if (!blob) {
                            this.zone.run(() => reject(new Error('Canvas to Blob conversion failed')));
                            return;
                        }
                        // Create a new file from the blob with the same name
                        const resizedFile = new File([blob], file.name, {
                            type: 'image/jpeg', // Standardize to JPEG for compression
                            lastModified: Date.now()
                        });
                        this.zone.run(() => resolve(resizedFile));
                    }, 'image/jpeg', quality);
                };

                img.onerror = (error) => this.zone.run(() => reject(error));
            };

            reader.onerror = (error) => this.zone.run(() => reject(error));
        }));
    }
}
