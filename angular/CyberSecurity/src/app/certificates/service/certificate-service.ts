import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private http: HttpClient) {}

  issue(certRequest: CertificateRequest) {
    return this.http.post(`${environment.apiHost}/certificates`, certRequest);
  }
}
