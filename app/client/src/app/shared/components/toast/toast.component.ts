import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-toast',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.toasts()" 
           class="toast" 
           [ngClass]="toast.type"
           (click)="toastService.remove(toast.id)">
        <span class="message">{{ toast.message }}</span>
        <span class="close">×</span>
      </div>
    </div>
  `,
    styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      pointer-events: none; /* Let clicks pass through container */
    }

    .toast {
      padding: 12px 20px;
      border-radius: 8px;
      color: white;
      min-width: 250px;
      max-width: 400px;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      pointer-events: auto; /* Enable clicks on toast */
      animation: slideIn 0.3s ease-out;
    }

    .toast.success { background-color: #10b981; } /* Green */
    .toast.error { background-color: #ef4444; }   /* Red */
    .toast.info { background-color: #3b82f6; }    /* Blue */

    .message {
      font-size: 0.95rem;
      font-weight: 500;
    }

    .close {
      margin-left: 15px;
      font-size: 1.2rem;
      opacity: 0.8;
    }

    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastComponent {
    constructor(public toastService: ToastService) { }
}
