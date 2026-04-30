import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CertificateRequest } from '../model/certificate-request';
import { CertificateResponse } from '../model/certificate-response';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  constructor(private http: HttpClient) {}

  issue(certRequest: CertificateRequest){
    return this.http.post(`${environment.apiHost}/certificates`, certRequest);
  }

  getAllParentCA() : Observable<CertificateResponse[]> {
    return this.http.get<CertificateResponse[]>(`${environment.apiHost}/get-parent-ca`);
  }
}
