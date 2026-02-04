import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AgencyService, Agency } from '../../../core/services/agency.service';
import { AgentService } from '../../../core/services/agent.service';
import { AuthService } from '../../../core/services/auth.service';
import { Agent } from '../../../core/models/agent.model';

@Component({
    selector: 'app-agency-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule],
    templateUrl: './agency-dashboard.component.html',
    styleUrls: ['./agency-dashboard.component.css']
})
export class AgencyDashboardComponent implements OnInit {
    agency: Agency | null = null;
    agents: Agent[] = [];
    isLoading = true;
    isEditing = false;
    agencyForm: FormGroup;
    error: string | null = null;
    successMessage: string | null = null;

    constructor(
        private agencyService: AgencyService,
        private agentService: AgentService,
        private fb: FormBuilder,
        public authService: AuthService,
        private cdr: ChangeDetectorRef
    ) {
        this.agencyForm = this.fb.group({
            name: ['', Validators.required],
            address: ['', Validators.required],
            phone: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]]
        });
    }

    ngOnInit() {
        this.loadDashboardData();
    }

    loadDashboardData() {
        this.isLoading = true;
        this.error = null;

        // Load Agency
        this.agencyService.getMyAgency().subscribe({
            next: (agency) => {
                this.agency = agency;
                this.agencyForm.patchValue(agency);

                // Load Agents only after we confirm we have an agency (though backend handles security)
                this.loadAgents();
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error loading agency:', err);
                this.error = 'Failed to load agency details.';
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    loadAgents() {
        this.agentService.getMyAgents().subscribe({
            next: (agents) => {
                this.agents = agents;
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error loading agents:', err);
                this.error = 'Failed to load agents.';
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    enableEdit() {
        this.isEditing = true;
        this.successMessage = null;
    }

    cancelEdit() {
        this.isEditing = false;
        if (this.agency) {
            this.agencyForm.patchValue(this.agency);
        }
    }

    updateAgency() {
        if (this.agencyForm.invalid) return;

        this.agencyService.updateMyAgency(this.agencyForm.value).subscribe({
            next: (agency) => {
                this.successMessage = 'Agency details updated successfully!';
                this.isEditing = false;
                // Update local object
                this.agency = { ...this.agency, ...this.agencyForm.value } as Agency;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error updating agency:', err);
                this.error = 'Failed to update agency details.';
                this.cdr.detectChanges();
            }
        });
    }
}
