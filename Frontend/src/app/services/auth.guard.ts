import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // Optional: Check for specific role requirements passed in the route data
  const expectedRoles = route.data['roles'] as Array<string>;
  if (expectedRoles) {
    const userRole = authService.getUserRole();
    if (!userRole || !expectedRoles.includes(userRole)) {
      router.navigate(['/dashboard']); // Redirect if role matches are invalid
      return false;
    }
  }

  return true;
};
