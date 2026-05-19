import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { RegistrationData } from '../model/RegistrationData';
import { LoginData } from '../model/LoginData';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly jwtHelper = new JwtHelperService();
  public readonly role = signal<string | null>(this.getRole());
  public readonly id = signal<number | null>(this.getId());

  login(data: LoginData) {
    return this.http.post<any>(`${environment.authHost}/login`, data, { observe: 'response' });
  }

  register(data: RegistrationData) {
    console.log(data);
    return this.http.post<any>(`${environment.authHost}/register`, data, { observe: 'response' });
  }

  registerCAUser(data: RegistrationData) {
    return this.http.post<any>(`${environment.authHost}/admin/create-user`, data, {
      observe: 'response',
    });
  }

  verify(token: string) {
    return this.http.get<any>(`${environment.authHost}/verify/${token}`, {
      observe: 'response',
    });
  }

  checkSetPassword(token: string) {
    return this.http.get<any>(`${environment.authHost}/set-password/${token}`);
  }

  setPassword(token: string, password: string) {
    return this.http.post<any>(
      `${environment.authHost}/set-password/${token}`,
      { password: password },
      { observe: 'response' },
    );
  }

  isLoggedIn() {
    const expiration = localStorage.getItem('expires_in');
    if (expiration) {
      const currentTime = Date.now();
      const isValid = currentTime < Number(expiration);
      return isValid;
    } else {
      return false;
    }
  }

  public getRole(): string | null {
    if (this.isLoggedIn()) {
      const accessToken = localStorage.getItem('jwt');
      if (accessToken) {
        try {
          const decodedToken = this.jwtHelper.decodeToken(accessToken);
          return decodedToken.type || null;
        } catch (error) {
          console.error('Error decoding token:', error);
          return null;
        }
      }
    }
    return null;
  }

  private getId(): number | null {
    if (this.isLoggedIn()) {
      const accessToken = localStorage.getItem('jwt');
      if (accessToken) {
        try {
          const decodedToken = this.jwtHelper.decodeToken(accessToken);
          return decodedToken.id || null;
        } catch (error) {
          console.error('Error decoding token:', error);
          return null;
        }
      }
    }
    return null;
  }
}
