import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Organization } from '../model/RegistrationData';

@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private readonly http = inject(HttpClient);

  getOrganizations() {
    return this.http.get<any>(`${environment.apiHost}/organizations`);
  }

  saveOrganization(org: Organization) {
    return this.http.post<any>(`${environment.apiHost}/save-organization`, org);
  }
}
