import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { RegistrationData } from '../model/RegistrationData';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  register(data: RegistrationData) {
    return this.http.post<any>(`${environment.authHost}/register`, data, { observe: 'response' });
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
}
