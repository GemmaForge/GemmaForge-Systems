import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { catchError, timeout } from 'rxjs/operators';
import { Observable, throwError, TimeoutError, BehaviorSubject } from 'rxjs';

export type MigrationStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface Migration {
  migrationId?: number;
  projectName: string;
  originalFileName?: string;
  originalCudaCode: string;
  hipifyOutputCode?: string;
  finalRocmCode: string;
  wavefrontBugsDetected: number;
  estimatedSavings: number;
  status: MigrationStatus;
  tokensUsed: number;
  securityAuditPassed?: boolean;
  vulnerabilityReport?: string;
  executionTimeMs?: number;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MigrationService {

  private readonly apiUrl = 'http://localhost:8080/api/migrations';

  // --- NEW: Shared state to pass real IDs from Dashboard to PR Approvals ---
  private pendingReportsSource = new BehaviorSubject<any[]>([]);
  pendingReports$ = this.pendingReportsSource.asObservable();

  constructor(private http: HttpClient) {}

  addPendingReport(report: any) {
    const current = this.pendingReportsSource.value;
    this.pendingReportsSource.next([...current, report]);
  }

  removePendingReport(reportId: number) {
    const current = this.pendingReportsSource.value;
    this.pendingReportsSource.next(current.filter(r => r.id !== reportId));
  }

  processMigration(projectName: string, fileName: string, cudaCode: string, tier: string): Observable<Migration> {
    const params = new HttpParams()
      .set('projectName', projectName)
      .set('fileName', fileName)
      .set('tier', tier);

    return this.http.post<Migration>(`${this.apiUrl}/process`, cudaCode, {
      params,
      headers: { 'Content-Type': 'text/plain' }
    }).pipe(
      timeout(180000),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse | TimeoutError | any) {
    let errorMessage = 'An unknown error occurred during migration computation.';
    if (error instanceof TimeoutError) {
      errorMessage = 'The AI inference engine timed out. The Master-tier model may still be processing.';
    } else if (error instanceof HttpErrorResponse) {
      if (error.error instanceof ErrorEvent) {
        errorMessage = `Network Connectivity Error: ${error.error.message}`;
      } else {
        const backendError = error.error?.error || 'Backend Processing Error';
        const backendDetails = error.error?.details || error.message;
        errorMessage = `${backendError}: ${backendDetails}`;
      }
    } else {
      errorMessage = error.message || errorMessage;
    }
    console.error('[GemmaForge Service Alert]', errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  getTotalSavings(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/analytics/total-savings`);
  }

  approveMigration(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/approve/${id}`, {});
  }

  getProjectsCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/analytics/projects-count`);
  }

  getProjectHistory(projectName: string): Observable<Migration[]> {
    return this.http.get<Migration[]>(`${this.apiUrl}/history?projectName=${projectName}`);
  }

  diagnoseCompilerError(rawLogs: string): Observable<{ explanation: string }> {
    return this.http.post<{ explanation: string }>(`${this.apiUrl}/diagnose`, rawLogs, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }
}
