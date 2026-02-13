import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { PropertyService } from '../../../core/services/property.service';
import { Property } from '../../../core/models/property.model';
import { Router, RouterLink, Event, NavigationEnd } from '@angular/router';
import { PropertyCardComponent } from '../../../shared/components/property-card/property-card.component';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
    selector: 'app-agent-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, PropertyCardComponent],
    templateUrl: './agent-dashboard.component.html',
    styleUrls: ['./agent-dashboard.component.css']
})
export class AgentDashboardComponent implements OnInit, OnDestroy {
    user: any;
    myProperties: Property[] = [];
    isLoading = true;
    private routerSubscription: Subscription;

    constructor(
        private authService: AuthService,
        private propertyService: PropertyService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) {
        this.routerSubscription = this.router.events.pipe(
            filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)
        ).subscribe(() => {
            if (this.user) {
                this.loadMyProperties();
            }
        });
    }

    ngOnInit() {
        this.user = this.authService.getUser();
        this.loadMyProperties();
    }

    ngOnDestroy() {
        if (this.routerSubscription) {
            this.routerSubscription.unsubscribe();
        }
    }

    loadMyProperties() {
        if (!this.user?.email) {
            this.isLoading = false;
            return;
        }

        this.propertyService.searchProperties({ agentEmail: this.user.email })
            .subscribe({
                next: (properties) => {
                    this.myProperties = properties;
                    this.isLoading = false;
                    this.cdr.detectChanges();
                },
                error: (error) => {
                    console.error('Error loading agent properties:', error);
                    this.isLoading = false;
                    this.cdr.detectChanges();
                }
            });
    }
}
