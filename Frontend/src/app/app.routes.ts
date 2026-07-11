import { Routes } from "@angular/router";
import { DashboardComponent } from "./components/dashboard/dashboard.component";
import { RocmAdvisorComponent } from "./components/rocm-advisor/rocm-advisor.component";
import { WavefrontVisualizerComponent } from './components/wavefront-visualizer/wavefront-visualizer.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from "./components/register/register.component";
import { PrApprovalsComponent } from "./components/pr-approvals/pr-approvals.component"; // <-- Add this import

import { authGuard } from './services/auth.guard';
import { roleGuard } from './services/role.guard'; // <-- Add this import

export const routes: Routes = [
  // Public Routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Protected Routes (Must be logged in)
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'rocm-advisor', component: RocmAdvisorComponent, canActivate: [authGuard] },
  { path: 'wavefront-visualizer', component: WavefrontVisualizerComponent, canActivate: [authGuard] },

  // Role-Protected Route (Must be logged in AND be a REVIEWER)
  {
    path: 'pr-approvals',
    component: PrApprovalsComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRole: 'REVIEWER' } // <-- Passes the required role to the guard
  },

  // Default and Wildcard Redirects
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
