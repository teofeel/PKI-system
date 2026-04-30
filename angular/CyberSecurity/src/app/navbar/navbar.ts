import { Component, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  public authService = inject(AuthService);
  private router = inject(Router);

  logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('expires_in');
    this.authService.role.set(null);
    this.authService.id.set(null);
    this.router.navigate(['/login']);
  }
}