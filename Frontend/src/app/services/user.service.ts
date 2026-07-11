import {inject, Injectable} from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from './auth.service';
export type UserRole = 'ENGINEER' | 'REVIEWER' ;

@Injectable({
  providedIn: 'root'
})
export class UserService {

  // Using BehaviorSubject so components automatically update when the role changes
  private currentRoleSubject = new BehaviorSubject<UserRole>('REVIEWER' || 'ENGINEER');
  currentRole$ = this.currentRoleSubject.asObservable();

  private currentUsernameSubject = new BehaviorSubject<string>('DevUser1');
  currentUsername$ = this.currentUsernameSubject.asObservable();

  setRole(role: UserRole, username: string) {
    this.currentRoleSubject.next(role);
    this.currentUsernameSubject.next(username);
  }

  getCurrentRole(): UserRole {
    return this.currentRoleSubject.value;
  }

  getCurrentUsername(): string {
    return this.currentUsernameSubject.value;
  }
}
