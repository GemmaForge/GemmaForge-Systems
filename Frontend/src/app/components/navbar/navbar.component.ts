import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import {RouterModule} from "@angular/router";

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  // Inject the real authentication service you just created
  authService = inject(AuthService);

  currentRole: string | null = '';
  username: string | null = '';

  ngOnInit(): void {
    // Read the actual user details saved in localStorage during login
    this.currentRole = this.authService.getUserRole();
    this.username = localStorage.getItem('email');
  }

  onLogout(): void {
    // Trigger the real JWT logout and redirect to the login screen
    this.authService.logout();
  }
}
