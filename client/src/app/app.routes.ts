import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    {
        path: 'auth',
        children: [
            { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
            { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) }
        ]
    },
    {
        path: 'properties',
        children: [
            { path: 'create', loadComponent: () => import('./features/properties/property-create/property-create.component').then(m => m.PropertyCreateComponent) },
            { path: '', loadComponent: () => import('./features/properties/property-list/property-list.component').then(m => m.PropertyListComponent) },
            { path: ':id', loadComponent: () => import('./features/properties/property-detail/property-detail.component').then(m => m.PropertyDetailComponent) }
        ]
    },
    {
        path: 'agents',
        children: [
            { path: 'dashboard', loadComponent: () => import('./features/agents/agent-dashboard/agent-dashboard.component').then(m => m.AgentDashboardComponent) },
            { path: '', loadComponent: () => import('./features/agents/agent-list/agent-list.component').then(m => m.AgentListComponent) },
            { path: ':id', loadComponent: () => import('./features/agents/agent-detail/agent-detail.component').then(m => m.AgentDetailComponent) }
        ]
    },
    {
        path: 'agency',
        children: [
            { path: 'dashboard', loadComponent: () => import('./features/agency/agency-dashboard/agency-dashboard.component').then(m => m.AgencyDashboardComponent) },
            { path: 'agents/create', loadComponent: () => import('./features/agency/agency-agent-create/agency-agent-create.component').then(m => m.AgencyAgentCreateComponent) }
        ]
    },
    { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
    { path: '**', redirectTo: '' }
];
