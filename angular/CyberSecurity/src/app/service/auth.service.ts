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
    console.log(`${environment.authHost}/register`);
    return this.http.post<any>(`${environment.authHost}/register`, data, { observe: 'response' });
  }
}
