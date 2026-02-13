import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './register.component.html',
    styleUrls: ['./auth.styles.css']
})
export class RegisterComponent implements OnInit {
    registerForm: FormGroup;
    error = '';
    isLoading = false;
    isAgency = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) {
        this.registerForm = this.fb.group({
            firstName: [''],
            lastName: [''],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', Validators.required],
            // Agency fields
            agencyName: [''],
            agencyEmail: [''],
            agencyPhone: [''],
            agencyAddress: ['']
        }, { validators: this.passwordMatchValidator });
    }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            this.isAgency = params['role'] === 'agency';
            this.updateValidators();
        });
    }

    updateValidators() {
        if (this.isAgency) {
            // Agency Managers need names + Agency details
            this.registerForm.get('firstName')?.setValidators(Validators.required);
            this.registerForm.get('lastName')?.setValidators(Validators.required);
            this.registerForm.get('agencyName')?.setValidators(Validators.required);
            this.registerForm.get('agencyEmail')?.setValidators([Validators.required, Validators.email]);
            this.registerForm.get('agencyPhone')?.setValidators(Validators.required);
            this.registerForm.get('agencyAddress')?.setValidators(Validators.required);
        } else {
            // Customers need names
            this.registerForm.get('firstName')?.setValidators(Validators.required);
            this.registerForm.get('lastName')?.setValidators(Validators.required);
            // Clear agency validators
            this.registerForm.get('agencyName')?.clearValidators();
            this.registerForm.get('agencyEmail')?.clearValidators();
            this.registerForm.get('agencyPhone')?.clearValidators();
            this.registerForm.get('agencyAddress')?.clearValidators();
        }

        // Update validity
        Object.keys(this.registerForm.controls).forEach(key => {
            this.registerForm.get(key)?.updateValueAndValidity();
        });
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('password')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    onSubmit() {
        if (this.registerForm.valid) {
            this.isLoading = true;
            this.error = '';

            const formValue = this.registerForm.value;

            if (this.isAgency) {
                // Map to backend expected format for Agency Registration
                const agencyData = {
                    agencyName: formValue.agencyName,
                    agencyEmail: formValue.agencyEmail,
                    agencyPhone: formValue.agencyPhone,
                    agencyAddress: formValue.agencyAddress,
                    managerFirstName: formValue.firstName,
                    managerLastName: formValue.lastName,
                    managerEmail: formValue.email,
                    password: formValue.password
                };

                this.authService.registerAgency(agencyData).subscribe({
                    next: () => {
                        this.router.navigate(['/auth/login'], { queryParams: { registered: 'agency' } });
                    },
                    error: (err: any) => {
                        console.error(err);
                        this.error = 'Agency registration failed. Emails might be already taken.';
                        this.isLoading = false;
                        this.cdr.detectChanges();
                    }
                });
            } else {
                // Customer Registration
                this.authService.register(formValue).subscribe({
                    next: () => {
                        this.router.navigate(['/auth/login'], { queryParams: { registered: true } });
                    },
                    error: (err: any) => {
                        console.error(err);
                        this.error = 'Registration failed. Email might be already taken.';
                        this.isLoading = false;
                        this.cdr.detectChanges();
                    }
                });
            }
        } else {
            this.registerForm.markAllAsTouched();
        }
    }
}
