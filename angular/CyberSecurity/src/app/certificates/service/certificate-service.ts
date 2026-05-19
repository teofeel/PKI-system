import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateResponse } from '../model/certificate-response';

import { Observable } from 'rxjs';
import { ExportKeystoreDTO } from '../model/export-keystore.dto';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private readonly http: HttpClient) { }

  issue(certRequest: CertificateRequest): Observable<string> {
    return this.http.post(`${environment.apiHost}/certificates`, certRequest, {
      responseType: 'text'
    });
  }

  fetchCertificatesByAccount(accountId: number | string): Observable<CertificateListItem[]> {
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
  getAllParentCA(): Observable<CertificateResponse[]> {
    return this.http.get<CertificateResponse[]>(`${environment.apiHost}/get-parent-ca`);
  }

  downloadCertificateNoPk(certId: number, format: string) {
    return this.http.get(`${environment.apiHost}/certificates/${certId}/export-no-pk`, {
      params: { format },
      observe: 'response',
      responseType: 'blob'
    });
  }

  exportKeystore(certId: number, password: string) {
    const body: ExportKeystoreDTO = { password };

    return this.http.post(`${environment.apiHost}/${certId}/export-keystore`, body, {
      observe: 'response',
      responseType: 'blob'
    });
  }
}
