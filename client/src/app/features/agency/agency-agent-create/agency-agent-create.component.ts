import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AgentService } from '../../../core/services/agent.service';

@Component({
    selector: 'app-agency-agent-create',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule],
    templateUrl: './agency-agent-create.component.html',
    styleUrls: ['./agency-agent-create.component.css']
})
export class AgencyAgentCreateComponent {
    agentForm: FormGroup;
    error: string | null = null;
    isLoading = false;

    constructor(
        private fb: FormBuilder,
        private agentService: AgentService,
        private router: Router
    ) {
        this.agentForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            phoneNumber: [''], // Optional
            biography: [''],
            birthDate: ['']
        });
    }

    onSubmit() {
        if (this.agentForm.invalid) return;

        this.isLoading = true;
        this.error = null;

        this.agentService.createAgent(this.agentForm.value).subscribe({
            next: () => {
                this.isLoading = false;
                this.router.navigate(['/agency/dashboard']);
            },
            error: (err) => {
                console.error('Error creating agent:', err);
                // backend might return text, or json error. 'createAgent' in service returns text observable but if it fails it throws error.
                this.error = 'Failed to create agent. Please try again.';
                this.isLoading = false;
            }
        });
    }
}
