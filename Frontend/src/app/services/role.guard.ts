import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Get the required role from the route's data object
  const expectedRole = route.data['expectedRole'];

  // Fetch the current user's role (adjust this method name if yours is different)
  const currentRole = authService.getUserRole();

  // If the roles match, allow navigation
  if (currentRole === expectedRole) {
    return true;
  }

  // If they don't match, redirect them back to the dashboard
  console.warn(`Access Denied: Requires ${expectedRole} role.`);
  router.navigate(['/dashboard']);
  return false;
};
