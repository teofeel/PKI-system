import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { GetAccountDetailsDTO } from '../model/GetAccountDetailsDTO';
import { Observable } from 'rxjs';
import { MfaQrCode } from '../model/MfaQrCode';
import { MfaCode } from '../model/MfaCode';

@Injectable({
  providedIn: 'root',
})
export class MfaService {
  private readonly http = inject(HttpClient);

  setupMfa(): Observable<MfaQrCode> {
    return this.http.post<MfaQrCode>(`${environment.apiHost}/mfa/setup`, {});
  }

  setupVerifyMfa(code: MfaCode): Observable<any> {
    return this.http.post(`${environment.apiHost}/mfa/setup/verify`, code );
  }

  removeVerifyMfa(code: MfaCode): Observable<any> {
    return this.http.put(`${environment.apiHost}/mfa/setup/verify`, code );
  }

  verifyMfa(code: MfaCode): Observable<any> {
    return this.http.post(`${environment.apiHost}/mfa/verify`, code );
  }
}