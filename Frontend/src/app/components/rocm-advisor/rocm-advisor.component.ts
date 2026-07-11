import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MigrationService } from '../../services/migration.service';

@Component({
  selector: 'app-rocm-advisor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rocm-advisor.component.html'
})
export class RocmAdvisorComponent {
  compilerLogs: string = '';
  diagnosisResult: string = '';
  isDiagnosing: boolean = false;

  constructor(private migrationService: MigrationService) {}

  diagnoseLogs(): void {
    if (!this.compilerLogs.trim()) return;

    this.isDiagnosing = true;
    this.migrationService.diagnoseCompilerError(this.compilerLogs).subscribe({
      next: (res: any) => {
        this.diagnosisResult = res.explanation;
        this.isDiagnosing = false;
      },
      error: () => {
        this.diagnosisResult = 'Failed to analyze logs.';
        this.isDiagnosing = false;
      }
    });
  }
}
