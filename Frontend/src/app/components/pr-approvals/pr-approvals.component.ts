import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { MigrationService } from '../../services/migration.service';
import { NgForOf, NgIf } from "@angular/common";

@Component({
  selector: 'app-pr-approvals',
  templateUrl: './pr-approvals.component.html',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  styleUrls: ['./pr-approvals.component.css']
})
export class PrApprovalsComponent implements OnInit {
  currentRole: string = '';
  isPushingPR: boolean = false;
  pendingReports: any[] = [];

  constructor(
    private authService: AuthService,
    private migrationService: MigrationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentRole = this.authService.getUserRole() || 'USER';

    if (this.currentRole === 'REVIEWER') {
      // --- FIXED: Subscribing to the real queue populated by the Dashboard ---
      this.migrationService.pendingReports$.subscribe(reports => {
        this.pendingReports = reports;
        this.cdr.detectChanges();
      });
    }
  }

  approveAndPush(reportId: number | undefined): void {
    if (reportId === undefined) return;
    const report = this.pendingReports.find(r => r.id === reportId);

    if (!report || !report.migrationId) {
      console.error("Migration ID not found");
      return;
    }

    this.isPushingPR = true;

    // This now sends the REAL id (e.g. 5) to your Java backend to trigger the GitHub push
    this.migrationService.approveMigration(report.migrationId).subscribe({
      next: () => {
        this.migrationService.removePendingReport(reportId);
        this.isPushingPR = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error("Approval failed at backend:", err);
        this.isPushingPR = false;
        this.cdr.detectChanges();
      }
    });
  }

  rejectReport(reportId: number | undefined): void {
    if (!reportId) return;
    this.migrationService.removePendingReport(reportId);
    this.cdr.detectChanges();
  }
}
