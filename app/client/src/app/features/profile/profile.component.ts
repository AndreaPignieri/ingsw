import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FileUploadService } from '../../core/services/file-upload.service';
import { ImageResizerService } from '../../core/services/image-resizer.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    template: `
    <div class="container page-content">
      <div class="profile-wrapper">
        <h1>My Profile</h1>
        
        <form [formGroup]="profileForm" (ngSubmit)="saveProfile()">
        <div class="profile-grid" [class.edit-mode]="isEditing" [class.is-agent]="isAgent()">
            
            <!-- LEFT COLUMN: Photo and Bio -->
            <div class="left-column profile-card" *ngIf="isAgent()">
                <div class="photo-container">
                    <img [src]="currentPhotoUrl" alt="Profile Photo" class="profile-photo" (error)="handleImageError($event)">
                    
                    <!-- Upload Overlay (Only in Edit Mode for Agents) -->
                    <div class="photo-overlay" *ngIf="isEditing && isAgent()">
                        <label for="fileInput" class="upload-btn">
                            <i class="fas fa-camera"></i> Change Photo
                        </label>
                        <input type="file" id="fileInput" (change)="onFileSelected($event)" accept="image/*" hidden>
                    </div>
                </div>

                <!-- Inline Error Message -->
                <div *ngIf="uploadError" class="upload-error-msg">
                    <small>{{ uploadError }}</small>
                </div>

                <div class="bio-section">
                    <h3>Biography</h3>
                    <ng-container *ngIf="isEditing && isAgent(); else viewBio">
                        <textarea formControlName="biography" class="form-control bio-input" rows="6" placeholder="Tell us about yourself..."></textarea>
                    </ng-container>
                    <ng-template #viewBio>
                         <p class="bio-text" *ngIf="user?.biography; else noBio">{{ user?.biography }}</p>
                        <ng-template #noBio>
                            <p class="text-muted italic">No biography provided.</p>
                        </ng-template>
                    </ng-template>
                </div>
            </div>

            <!-- RIGHT COLUMN: Personal Details -->
            <div class="right-column photo-card">
                    
                    <!-- View Mode Details -->
                    <div *ngIf="!isEditing">
                        <div class="info-group">
                            <label>First Name</label>
                            <p>{{ user?.firstName }}</p>
                        </div>
                        <div class="info-group">
                            <label>Last Name</label>
                            <p>{{ user?.lastName }}</p>
                        </div>
                        <div class="info-group" *ngIf="!isOAuthUser()">
                            <label>Email</label>
                            <p>{{ user?.email }}</p>
                        </div>
                         <div class="info-group" *ngIf="isOAuthUser()">
                            <label>Account Status</label>
                            <div style="margin-top: 0.5rem;">
                                 <span class="badge-oauth">
                                    Connected via {{ user?.authProvider }}
                                </span>
                            </div>
                        </div>
                         <div class="info-group" *ngIf="!isOAuthUser()">
                            <label>Password</label>
                            <p class="password-dots">••••••••</p>
                        </div>

                        <div class="action-buttons">
                            <button type="button" class="btn btn-primary full-width" (click)="enableEdit()">Modify Profile</button>
                            <button type="button" class="btn btn-secondary full-width" (click)="logout()">Logout</button>
                        </div>
                    </div>

                    <!-- Edit Mode Form -->
                    <div *ngIf="isEditing">
                        <div class="info-group">
                            <label>First Name</label>
                            <input type="text" formControlName="firstName" class="form-control" [readonly]="isOAuthUser()">
                        </div>
                        <div class="info-group">
                            <label>Last Name</label>
                            <input type="text" formControlName="lastName" class="form-control" [readonly]="isOAuthUser()">
                        </div>
                        <div class="info-group" *ngIf="!isOAuthUser()">
                            <label>Email</label>
                            <input type="email" formControlName="email" class="form-control">
                        </div>

                        <div class="info-group" *ngIf="isOAuthUser()">
                             <label>Account Linked With</label>
                             <div style="margin-top: 0.5rem;">
                                <span class="badge-oauth">{{ user.authProvider }}</span>
                             </div>
                        </div>

                        <!-- Password Change Section -->
                        <div class="info-group" *ngIf="!isChangingPassword && (!user?.authProvider || user?.authProvider === 'LOCAL')">
                            <label>Password</label>
                            <div class="static-password-group">
                                <p class="password-dots">••••••••</p>
                                <a class="modify-link" (click)="startPasswordChange()">Modify Password?</a>
                            </div>
                        </div>

                        <div class="password-section" *ngIf="isChangingPassword">
                            <div class="password-header">
                                <h3>Change Password</h3>
                                <button type="button" class="btn-icon" (click)="cancelPasswordChange()">✕</button>
                            </div>
                            <div class="info-group password-group">
                                <label>Current Password</label>
                                <input type="password" formControlName="oldPassword" class="form-control">
                            </div>
                            <div class="info-group password-group">
                                <label>New Password</label>
                                <input type="password" formControlName="password" class="form-control">
                            </div>
                        </div>

                        <div class="button-group">
                            <button type="submit" class="btn btn-primary" [disabled]="profileForm.invalid || isLoading">
                                {{ isLoading ? 'Saving...' : 'Save Changes' }}
                            </button>
                            <button type="button" class="btn btn-outline" (click)="cancelEdit()">
                                {{ isOAuthUser() ? 'Close' : 'Cancel' }}
                            </button>
                        </div>
                    </div>
            </div>

        </div>
        </form>
      </div>
    </div>
  `,
    styles: [`
    .profile-wrapper {
        width: 100%;
        margin-top: 2rem;
        max-width: 1000px;
        margin-left: auto;
        margin-right: auto;
    }
    h1 {
        margin-bottom: 2rem;
        color: #1f2937;
        text-align: center;
    }
    
    /* 2-Column Grid Layout */
    .profile-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 2rem;
        max-width: 600px; /* Constrain width for non-agents */
        margin: 0 auto;   /* Center the grid */
    }
    @media (min-width: 768px) {
        .profile-grid.is-agent {
            grid-template-columns: 300px 1fr;
            /* align-items: stretch; is default, allowing equal height columns */
            max-width: 100%; /* Reset width for agents to use full space */
        }
    }

    /* Left Column */
    .left-column {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2rem;
        /* height: fit-content; Removed to allow it to fill height */
        background: white; /* Added for card style */
        padding: 2.5rem;   /* Added for card style */
        border-radius: 12px; /* Added for card style */
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); /* Added for card style */
    }

    /* Photo Container & Overlay */
    .photo-container {
        position: relative;
        width: 200px;
        height: 200px;
        border-radius: 50%;
        overflow: hidden;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        border: 4px solid white;
    }
    .profile-photo {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .photo-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0, 0, 0, 0.6);
        padding: 0.5rem;
        text-align: center;
        transition: opacity 0.2s;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 40px;
    }
    .upload-btn {
        color: white;
        cursor: pointer;
        font-size: 0.875rem;
        font-weight: 500;
        margin: 0;
    }
    .upload-btn:hover {
        text-decoration: underline;
    }
    .upload-error-msg {
        color: #ef4444;
        text-align: center;
        margin-top: -1rem; /* Pull closer to photo */
        margin-bottom: 1rem;
        max-width: 200px;
    }

    /* Bio Section */
    .bio-section {
        width: 100%;
        text-align: center;
    }
    .bio-section h3 {
        font-size: 1.1rem;
        color: #4b5563;
        margin-bottom: 0.5rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    .bio-text {
        font-size: 1rem;
        color: #374151;
        line-height: 1.6;
        white-space: pre-wrap;
    }
    .bio-input {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        font-size: 0.95rem;
        line-height: 1.5;
    }
    .italic { font-style: italic; }

    /* Right Column (Card) */
    .photo-card {
        background: white;
        padding: 2.5rem;
        border-radius: 12px;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    }

    .info-group {
        margin-bottom: 1.5rem;
        border-bottom: 1px solid #f3f4f6;
        padding-bottom: 1rem;
    }
    .info-group:last-of-type { border-bottom: none; }

    label {
        font-weight: 600;
        color: #6b7280;
        font-size: 0.75rem;
        display: block;
        margin-bottom: 0.25rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    p {
        font-size: 1.125rem;
        margin: 0;
        color: #111827;
        font-weight: 500;
    }
    
    .form-control {
        width: 100%;
        padding: 0.5rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 1rem;
    }
    
    .badge-oauth {
        background: #eef2ff; 
        color: #4f46e5; 
        padding: 6px 14px; 
        border-radius: 999px; 
        font-size: 0.875rem; 
        font-weight: 500;
    }
    .password-dots {
        font-family: monospace; 
        letter-spacing: 2px;
    }

    .action-buttons {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        margin-top: 2rem;
    }
    .button-group {
        display: flex;
        gap: 1rem;
        margin-top: 2rem;
    }
    .btn {
        padding: 0.75rem 1.5rem;
        border-radius: 6px;
        font-weight: 500;
        cursor: pointer;
        border: none;
        transition: background-color 0.2s;
        text-align: center;
    }
    .btn:disabled { opacity: 0.6; cursor: not-allowed; }
    .btn-primary { background-color: #2563eb; color: white; }
    .btn-primary:hover { background-color: #1d4ed8; }
    .btn-secondary { background-color: #f3f4f6; color: #374151; }
    .btn-secondary:hover { background-color: #e5e7eb; }
    .btn-outline { background-color: white; border: 1px solid #d1d5db; color: #374151; }
    .btn-outline:hover { background-color: #f9fafb; }
    .full-width { width: 100%; }

    /* Password Section */
    .password-section {
        background: #f9fafb;
        padding: 1rem;
        border-radius: 8px;
        margin-bottom: 1.5rem;
        border: 1px solid #e5e7eb;
    }
    .password-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }
    .password-header h3 { margin: 0; font-size: 1rem; }
    .btn-icon { background: none; border: none; cursor: pointer; color: #6b7280; font-size: 1.25rem; }
    .static-password-group { display: flex; align-items: center; justify-content: space-between; }
    .modify-link { color: #2563eb; font-size: 0.875rem; font-weight: 500; cursor: pointer; }
    .modify-link:hover { text-decoration: underline; }
  `]
})
export class ProfileComponent implements OnInit {
    user: any = null;
    isEditing = false;
    isLoading = false;
    isChangingPassword = false;
    profileForm: FormGroup;
    uploadError: string | null = null;

    constructor(
        private authService: AuthService,
        private userService: UserService,
        private router: Router,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef,
        private fileUploadService: FileUploadService,
        private imageResizer: ImageResizerService,
        private toastService: ToastService
    ) {
        this.profileForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            password: [''],
            oldPassword: [''],
            biography: [''],
            profilePhoto: ['']
        });
    }

    ngOnInit() {
        this.loadUser();
    }

    isOAuthUser(): boolean {
        return this.user?.authProvider && this.user.authProvider !== 'LOCAL';
    }

    isAgent(): boolean {
        // Only agents can have bio/photo
        // If we want allow regular users to have photos too, remove this check for photo parts
        return this.user?.role === 'AGENT';
    }

    loadUser() {
        this.user = this.authService.getUser();
        if (!this.user) {
            const userStr = localStorage.getItem('user');
            if (userStr) {
                this.user = JSON.parse(userStr);
            }
        }
        console.log('Profile User Loaded:', this.user);
    }

    // Helper to get photo URL for display (Form value > User value > Default)
    get currentPhotoUrl(): string {
        const formValue = this.profileForm.get('profilePhoto')?.value;
        if (formValue) return formValue;
        if (this.user?.profilePhoto) return this.user.profilePhoto;
        return 'assets/default-avatar.png';
    }

    handleImageError(event: any) {
        // Use a simple SVG placeholder data URI to guarantee it loads
        const placeholder = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iI2NjYyIgZD0iTTEyIDJhMTAgMTAgMCAxIDAgMTAgMTBBMTAgMTAgMCAwIDAgMTIgMnptMCA1YTMgMyAwIDEgMS0zIDMgMyAzIDAgMCAxIDMgM3ptMCAxNC4yYTcuMiA3LjIgMCAwIDEtNi0zLjJjMC0yLjYgNS4yLTQgNi00czYgMS40IDYgNC4wYTcuMiA3LjIgMCAwIDEtNiAzLjJ6Ii8+PC9zdmc+';
        event.target.src = placeholder;
        event.target.onerror = null;
    }

    enableEdit() {
        this.isEditing = true;
        this.isChangingPassword = false;
        this.profileForm.patchValue({
            firstName: this.user.firstName,
            lastName: this.user.lastName,
            email: this.user.email,
            password: '',
            oldPassword: '',
            biography: this.user.biography || '',
            profilePhoto: this.user.profilePhoto || ''
        });
    }

    cancelEdit() {
        this.isEditing = false;
        this.isChangingPassword = false;
        this.profileForm.reset();
    }

    onFileSelected(event: any) {
        const file: File = event.target.files[0];
        if (file) {
            this.isLoading = true;
            this.uploadError = null;

            // Resize image to max 800x800, quality 0.8
            this.imageResizer.resizeImage(file, 800, 800, 0.8).subscribe({
                next: (resizedFile) => {
                    this.fileUploadService.uploadFile(resizedFile).subscribe({
                        next: (url) => {
                            // Append cache buster
                            const fullUrl = url + '?t=' + new Date().getTime();
                            this.profileForm.patchValue({ profilePhoto: fullUrl });

                            // Explicitly mark as dirty and update validity to ensure Save button enables
                            this.profileForm.get('profilePhoto')?.markAsDirty();
                            this.profileForm.get('profilePhoto')?.markAsTouched();
                            this.profileForm.updateValueAndValidity();

                            this.isLoading = false;
                            this.cdr.detectChanges();
                        },
                        error: (err) => {
                            console.error('Upload failed', err);
                            const errorMessage = err.error?.error || 'Failed to upload image. Please try again.';
                            this.uploadError = errorMessage;
                            this.isLoading = false;
                            this.cdr.detectChanges();
                        }
                    });
                },
                error: (err) => {
                    console.error('Resize failed', err);
                    this.uploadError = 'Failed to process image.';
                    this.isLoading = false;
                    this.cdr.detectChanges();
                }
            });
        }
    }

    startPasswordChange() {
        this.isChangingPassword = true;
    }

    cancelPasswordChange() {
        this.isChangingPassword = false;
        this.profileForm.get('password')?.reset();
        this.profileForm.get('oldPassword')?.reset();
    }

    saveProfile() {
        if (this.profileForm.invalid) return;

        const formData = this.profileForm.value;

        if (!this.isChangingPassword) {
            delete formData.password;
            delete formData.oldPassword;
        } else {
            if (!formData.oldPassword || !formData.password) {
                alert('Both current and new passwords are required.');
                return;
            }
        }

        this.isLoading = true;
        this.userService.updateProfile(formData).subscribe({
            next: () => {
                this.user = {
                    ...this.user,
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    email: formData.email,
                    biography: formData.biography,
                    profilePhoto: formData.profilePhoto
                };
                localStorage.setItem('user', JSON.stringify(this.user));
                this.isEditing = false;
                this.isChangingPassword = false;
                this.isLoading = false;
                this.toastService.show('Profile updated successfully!', 'success');
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Failed to update profile', err);
                this.isLoading = false;
                this.toastService.show('Failed to update profile', 'error');
                this.cdr.detectChanges();
            }
        });
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/']);
    }
}
