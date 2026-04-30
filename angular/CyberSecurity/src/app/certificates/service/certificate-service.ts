import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';
import { Observable } from 'rxjs';
import { CertificateListItem } from '../model/certificate-item-list';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private http: HttpClient) {}

  issue(certRequest: CertificateRequest) {
    return this.http.post(`${environment.apiHost}/certificates`, certRequest);
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
