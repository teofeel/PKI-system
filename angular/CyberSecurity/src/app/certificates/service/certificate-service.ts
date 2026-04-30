import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';
import { CertificateListItem } from '../model/certificate-item-list';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private http: HttpClient) {}

  issue(certRequest: CertificateRequest) {
    return this.http.post(`${environment.apiHost}/certificates`, certRequest);
  }

  fetchCertificatesByAccount(accountId: number | string) : Observable<CertificateListItem[]> {
    return this.http.get<CertificateListItem[]>(`${environment.apiHost}/accounts/${accountId}/certificates`);
  }
  
  getAdminCertificates(): Observable<CertificateListItem[]> {
    return this.http.get<CertificateListItem[]>(`${environment.apiHost}/certificates/admin`);
  }

  getChildren(certId: number): Observable<CertificateListItem[]> {
    return this.http.get<CertificateListItem[]>(
      `${environment.apiHost}/certificates/${certId}/children`,
    );
  }
}
