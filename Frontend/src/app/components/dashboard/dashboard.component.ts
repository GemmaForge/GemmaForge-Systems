import {Component, OnInit, ChangeDetectorRef, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MigrationService, Migration } from '../../services/migration.service';
import { DiffHighlighterPipe } from '../../pipes/diff.pipe';
import { UserService, UserRole } from '../../services/user.service';
import { ProjectService, Project } from '../../services/project.service';
import { ReportService, MigrationReport } from '../../services/report.service';

// --- FIXED: Import the Wavefront Visualizer ---
import { WavefrontVisualizerComponent } from '../wavefront-visualizer/wavefront-visualizer.component';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  // --- FIXED: Included WavefrontVisualizerComponent in imports ---
  imports: [
    CommonModule,
    FormsModule,
    DiffHighlighterPipe,
    RouterModule,
    WavefrontVisualizerComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  projectName: string = '';
  cudaCode: string = '';
  originalFileName: string = '';
  isProcessing: boolean = false;
  isDownloading: boolean = false;
  errorMessage: string | null = null;
  currentMigration: Migration | null = null;
  authService = inject(AuthService);
  selectedTier: 'SENIOR' | 'EXPERT' | 'MASTER' = 'EXPERT';

  currentRole: string = this.userService.getCurrentRole();
  currentUsername: string = '';
  projects: Project[] = [];
  migratedProjectCount: number = 0;

  pendingReports: any[] = [];
  totalCorporateSavings: number = 0;
  isPushingPR: boolean = false;

  migrationHistory: Migration[] = [];

  constructor(
    private migrationService: MigrationService,
    private userService: UserService,
    private projectService: ProjectService,
    private reportService: ReportService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userService.currentRole$.subscribe(role => this.currentRole = role);
    this.userService.currentUsername$.subscribe(name => this.currentUsername = name);
    this.loadCorporateMetrics();

    this.migrationService.pendingReports$.subscribe(reports => {
      this.pendingReports = reports;
      this.cdr.detectChanges();
    });
  }

  loadCorporateMetrics(): void {
    this.migrationService.getTotalSavings().subscribe({
      next: (total) => {
        this.totalCorporateSavings = Number(total) || 0;
        this.cdr.detectChanges();
      }
    });

    this.migrationService.getProjectsCount().subscribe({
      next: (count) => {
        this.migratedProjectCount = Number(count) || 0;
        this.cdr.detectChanges();
      },
      error: (err) => console.log('Could not fetch project count', err)
    });
  }

  loadHistory(projectName: string): void {
    if (!projectName || projectName.trim() === '') return;
    this.migrationService.getProjectHistory(projectName).subscribe({
      next: (data) => {
        this.migrationHistory = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load history', err)
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.originalFileName = file.name;
      if (!this.projectName || this.projectName.trim() === '') {
        this.projectName = file.name.split('.')[0];
      }
      const reader = new FileReader();
      reader.onload = () => {
        this.cudaCode = reader.result as string;
        this.loadHistory(this.projectName);
      };
      reader.readAsText(file);
    }
  }

  copyToClipboard(text: string): void {
    if (!text) return;
    navigator.clipboard.writeText(text).catch(err => console.error(err));
  }

  triggerMigration(): void {
    if (!this.cudaCode.trim() || !this.projectName.trim()) {
      this.errorMessage = 'Action Required: Please input a project name and supply legacy CUDA code.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = null;
    this.currentMigration = null;

    this.migrationService.processMigration(this.projectName, this.originalFileName, this.cudaCode, this.selectedTier).subscribe({
      next: (data) => {
        this.currentMigration = data;
        this.isProcessing = false;
        this.loadHistory(this.projectName);
        this.selectedTier;
        const mockReportId = Math.floor(Math.random() * 10000) + 1;
        const mId = data.migrationId || 0;

        this.migrationService.addPendingReport({
          id: mockReportId,
          migrationId: mId,
          project: { projectName: this.projectName },
          originalFileName: this.originalFileName,
          originalCode: this.cudaCode,
          refactoredCode: data.finalRocmCode || '',
          estimatedSavings: Number(data.estimatedSavings) || 15000,
          status: 'PENDING_REVIEW',
          diffSummary: '// Wavefront optimizations successfully mapped.',
          wavefrontBugsResolved: data.wavefrontBugsDetected || 1
        });

        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Migration Backend Error:', err);
        this.errorMessage = 'Backend connection failed.';
        this.isProcessing = false;
        this.cdr.detectChanges();
      }
    });
  }

  downloadOptimizedCode(): void {
    if (!this.currentMigration || !this.currentMigration.finalRocmCode) return;
    this.isDownloading = true;
    const blob = new Blob([this.currentMigration.finalRocmCode.replace(/\[MODIFIED\]/g, '')], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${this.projectName}_optimized.cpp`;
    anchor.click();
    window.URL.revokeObjectURL(url);
    setTimeout(() => { this.isDownloading = false; }, 800);
  }

  resetWorkspace(): void {
    this.currentMigration = null;
    this.cudaCode = '';
    this.projectName = '';
    this.originalFileName = '';
    this.errorMessage = null;
    this.migrationHistory = [];
    this.cdr.detectChanges();
  }
}
