import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Property } from '../../../core/models/property.model';

@Component({
    selector: 'app-property-card',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './property-card.component.html',
    styleUrls: ['./property-card.component.css']
})
export class PropertyCardComponent {
    @Input() property!: Property;
}
