import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './login.component.html',
    styleUrls: ['./auth.styles.css']
})
export class LoginComponent implements OnInit {
    loginForm: FormGroup;
    error = '';
    isLoading = false;
    submitted = false;

    loginTitle = 'Welcome Back';
    loginRole: 'customer' | 'agent' | 'manager' = 'customer';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', Validators.required]
        });
    }

    ngOnInit() {
        // Handle Role Query Param
        this.route.queryParams.subscribe(params => {
            const role = params['role'];
            if (role === 'agent') {
                this.loginRole = 'agent';
                this.loginTitle = 'Agent Login';
            } else if (role === 'manager') {
                this.loginRole = 'manager';
                this.loginTitle = 'Agency Manager Login';
            } else {
                this.loginRole = 'customer';
                this.loginTitle = 'Welcome Back';
            }
        });

        // Check for token from OAuth2 redirect
        const token = this.route.snapshot.queryParamMap.get('token');
        if (token) {
            this.handleLoginSuccess(token);
        }

        const error = this.route.snapshot.queryParamMap.get('error');
        if (error) {
            this.error = 'Login failed: ' + error;
        }
    }

    onSubmit() {
        this.submitted = true;
        if (this.loginForm.valid) {
            this.isLoading = true;
            this.error = '';

            const loginData = {
                ...this.loginForm.value
            };

            this.authService.login(loginData).subscribe({
                next: (response: any) => {
                    this.handleLoginSuccess(response.token, response.user);
                },
                error: (err: any) => {
                    console.error(err);
                    this.error = 'Invalid email or password';
                    // Show specific role error if possible, but generic message is safer
                    if (this.loginRole !== 'customer') {
                        this.error = `Login failed. Ensure you have ${this.loginRole} permissions.`;
                    }
                    this.isLoading = false;
                    this.cdr.detectChanges();
                }
            });
        }
    }

    private handleLoginSuccess(token: string, user?: any) {
        localStorage.setItem('token', token);

        if (user) {
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            // Fallback if user object not provided (e.g. from oauth redirect param)
            try {
                const payload = token.split('.')[1];
                const decoded = JSON.parse(atob(payload));
                const decodedUser = {
                    firstName: decoded.firstName || '',
                    lastName: decoded.lastName || '',
                    email: decoded.sub,
                    role: decoded.role || 'USER',
                    authProvider: decoded.authProvider || null
                };
                localStorage.setItem('user', JSON.stringify(decodedUser));
            } catch (e) {
                console.error('Failed to decode token', e);
            }
        }

        const currentUser = JSON.parse(localStorage.getItem('user') || '{}');

        // Debug
        console.log('Login success, redirecting. User role:', currentUser.role);

        if (currentUser.role === 'AGENT') {
            this.router.navigate(['/agents/dashboard']);
        } else if (currentUser.role === 'AGENCY') {
            this.router.navigate(['/']);
        } else {
            this.router.navigate(['/']);
        }
    }
    navigateToRegister(event: Event) {
        event.preventDefault();
        this.router.navigate(['/auth/register']);
    }
}
