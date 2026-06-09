import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';
import { CertificateApprovalItem, CertificateListItem } from '../model/certificate-item-list';
import { CertificateResponse } from '../model/certificate-response';

import { Observable } from 'rxjs';
import { ExportKeystoreDTO } from '../model/export-keystore.dto';
import { OrganizationDropdownDTO } from '../model/organization-dropdown-item';
import { CertificateDropdownListDTO } from '../model/certificate-dropdown-item';
import { AutoGenerateRequestDTO } from '../model/auto-generate-request';

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

  exportKeystore(certId: number, password: string, format: string, alias: string) {
    const body: ExportKeystoreDTO = { 
      pkcs12: format === 'p12',
      alias: alias,
      password: password };

    return this.http.post(`${environment.apiHost}/${certId}/export-keystore`, body, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  getAllCA(): Observable<CertificateDropdownListDTO[]>{
    return this.http.get<CertificateDropdownListDTO[]>(`${environment.apiHost}/certificates/ca`);
  }

  generateEECert(data: AutoGenerateRequestDTO){
    return this.http.post(`${environment.apiHost}/certificates/ee/autogenerate`, data, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  generateCsrEECert(formData: FormData): Observable<number>{
    return this.http.post<number>(`${environment.apiHost}/certificates/ee/csr`, formData)
  }

  getPendingCertificates(): Observable<CertificateApprovalItem[]> {
    return this.http.get<CertificateApprovalItem[]>(`${environment.apiHost}/certificates/pending`);
  }

  getRejectedCertificates(): Observable<CertificateApprovalItem[]> {
    return this.http.get<CertificateApprovalItem[]>(`${environment.apiHost}/certificates/rejected`);
  }

  getInactiveCertificates(): Observable<CertificateApprovalItem[]> {
    return this.http.get<CertificateApprovalItem[]>(`${environment.apiHost}/certificates/inactive`);
  }

  approveCertificate(id: number): Observable<CertificateApprovalItem> {
    return this.http.post<CertificateApprovalItem>(`${environment.apiHost}/certificates/${id}/approve`, {});
  }

  rejectCertificate(id: number, reason?: string): Observable<CertificateApprovalItem> {
    return this.http.post<CertificateApprovalItem>(`${environment.apiHost}/certificates/${id}/reject`, {
      certificateId: id,
      rejectionReason: reason || null
    });
  }
}
