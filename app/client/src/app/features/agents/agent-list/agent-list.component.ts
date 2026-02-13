import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AgentService } from '../../../core/services/agent.service';
import { Agent } from '../../../core/models/agent.model';

@Component({
    selector: 'app-agent-list',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './agent-list.component.html',
    styleUrls: ['./agent-list.component.css']
})
export class AgentListComponent implements OnInit {
    agents: Agent[] = [];

    constructor(
        private agentService: AgentService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.agentService.getAgents().subscribe({
            next: (agents) => {
                this.agents = agents;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Failed to load agents', err);
                // Fallback to empty list or show error message
                this.agents = [];
                this.cdr.detectChanges();
            }
        });
    }
}
