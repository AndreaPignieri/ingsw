
import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-dual-range-slider',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './dual-range-slider.component.html',
    styleUrls: ['./dual-range-slider.component.css']
})
export class DualRangeSliderComponent implements OnChanges {
    @Input() min: number = 0;
    @Input() max: number = 1000;
    @Input() step: number = 1;
    @Input() currentMin: number | undefined;
    @Input() currentMax: number | undefined;
    @Input() label: string = 'Range';
    @Input() formatValue: (v: number) => string = (v) => v.toString();

    @Output() currentMinChange = new EventEmitter<number>();
    @Output() currentMaxChange = new EventEmitter<number>();
    @Output() changeEnd = new EventEmitter<void>();

    minValue: number = 0;
    maxValue: number = 1000;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['min'] || changes['max'] || changes['currentMin'] || changes['currentMax']) {
            this.updateValues();
        }
    }

    updateValues() {
        this.minValue = this.currentMin !== undefined ? this.currentMin : this.min;
        this.maxValue = this.currentMax !== undefined ? this.currentMax : this.max;
    }

    onMinChange() {
        if (this.minValue > this.maxValue) {
            this.minValue = this.maxValue;
        }
        this.currentMinChange.emit(this.minValue);
    }

    onMaxChange() {
        if (this.maxValue < this.minValue) {
            this.maxValue = this.minValue;
        }
        this.currentMaxChange.emit(this.maxValue);
    }

    onChangeEnd() {
        this.changeEnd.emit();
    }

    get minPercent(): number {
        return ((this.minValue - this.min) / (this.max - this.min)) * 100;
    }

    get maxPercent(): number {
        return ((this.maxValue - this.min) / (this.max - this.min)) * 100;
    }
}
