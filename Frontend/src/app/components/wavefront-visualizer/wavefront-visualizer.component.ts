import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-wavefront-visualizer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wavefront-visualizer.component.html',
  styleUrls: ['./wavefront-visualizer.component.css']
})
export class WavefrontVisualizerComponent implements OnInit {
  @Input() bugsResolved: number = 0;

  // Arrays representing threads, naturally indexed 0-31 and 0-63
  cudaWarp: number[] = Array.from({ length: 32 }, (_, i) => i);
  rocmWavefront: number[] = Array.from({ length: 64 }, (_, i) => i);

  isAnimating: boolean = false;
  activeLane: number | null = null;

  ngOnInit(): void {
    this.triggerPulseAnimation();
  }

  triggerPulseAnimation(): void {
    this.isAnimating = true;
    setTimeout(() => {
      this.isAnimating = false;
    }, 2000);
  }

  // Captures the exact index (0-63)
  hoverLane(index: number): void {
    this.activeLane = index;
  }

  clearHover(): void {
    this.activeLane = null;
  }
}
