import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MigrationService, Migration } from '../../services/migration.service';
import { DiffHighlighterPipe } from '../../pipes/diff.pipe';
import { UserService, UserRole } from '../../services/user.service';
import { ProjectService, Project } from '../../services/project.service';
import { ReportService, MigrationReport } from '../../services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DiffHighlighterPipe, RouterModule],
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

  // --- FIXED: Added the missing selectedTier property ---
  selectedTier: 'SENIOR' | 'EXPERT' | 'MASTER' = 'EXPERT';

  currentRole: UserRole = 'ENGINEER';
  currentUsername: string = '';
  projects: Project[] = [];
  migratedProjectCount: number = 0;

  pendingReports: MigrationReport[] = [];
  totalCorporateSavings: number = 0;
  isPushingPR: boolean = false;

  // --- FIXED: Added history array for the Corporate Dashboard ---
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

  // --- FIXED: Added the history fetcher ---
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

  approveAndPush(reportId: number | undefined): void {
    if (reportId === undefined) return;

    const report = this.pendingReports.find(r => r.id === reportId);
    if (!report) return;

    if (!report.migrationId) {
      console.error("No Database ID found for this report.");
      return;
    }

    this.isPushingPR = true;

    this.migrationService.approveMigration(report.migrationId).subscribe({
      next: () => {
        this.pendingReports = this.pendingReports.filter(r => r.id !== reportId);
        this.loadCorporateMetrics();
        if (this.projectName) this.loadHistory(this.projectName);
        this.isPushingPR = false;
      },
      error: (err: any) => {
        console.error("Approval failed:", err);
        this.isPushingPR = false;
      }
    });
  }

  rejectReport(reportId: number | undefined): void {
    if (!reportId) return;
    this.pendingReports = this.pendingReports.filter(r => r.id !== reportId);
    this.cdr.detectChanges();
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
        this.loadHistory(this.projectName); // Load history automatically
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

    // Passes the 4 required arguments (including selectedTier)
    this.migrationService.processMigration(this.projectName, this.originalFileName, this.cudaCode, this.selectedTier).subscribe({
      next: (data) => {
        this.currentMigration = data;
        this.isProcessing = false;

        this.loadHistory(this.projectName); // Refresh history

        const mockReportId = Math.floor(Math.random() * 10000) + 1;
        const mId = data.migrationId || 0;

        this.pendingReports.push({
          id: mockReportId,
          migrationId: mId,
          project: { projectName: this.projectName } as Project,
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
