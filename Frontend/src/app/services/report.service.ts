import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project } from './project.service';

export interface MigrationReport {
  id?: number;
  project?: Project;
  originalFileName: string;
  originalCode: string;
  refactoredCode: string;
  diffSummary: string;
  estimatedSavings: number;
  wavefrontBugsResolved: number;
  status: 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  migrationId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getPendingReports(): Observable<MigrationReport[]> {
    return this.http.get<MigrationReport[]>(`${this.apiUrl}/pending`);
  }

  getReportsByProject(projectId: number): Observable<MigrationReport[]> {
    return this.http.get<MigrationReport[]>(`${this.apiUrl}/project/${projectId}`);
  }

  reviewReport(reportId: number, status: string, reviewerUsername: string): Observable<MigrationReport> {
    let params = new HttpParams()
      .set('status', status)
      .set('reviewerUsername', reviewerUsername);

    return this.http.patch<MigrationReport>(`${this.apiUrl}/${reportId}/review`, null, { params });
  }
}
