import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive],
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
    isMenuOpen = false;

    constructor(public authService: AuthService, private router: Router) {
        console.log('Navbar Debug: Current User:', this.authService.getUser());
        console.log('Navbar Debug: Is Agent?', this.authService.isAgent());
        console.log('Navbar Debug: Is Agency?', this.authService.isAgency());
        const user = this.authService.getUser();
        if (user) {
            console.log('Navbar Debug: User Role:', user.role);
        }
    }

    toggleMenu() {
        this.isMenuOpen = !this.isMenuOpen;
    }

    navigateToRegister() {
        this.router.navigate(['/auth/register']);
        this.isMenuOpen = false; // Close mobile menu if open
    }
}
