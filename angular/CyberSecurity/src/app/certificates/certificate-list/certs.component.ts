import { Component } from '@angular/core';
import { CertificateList } from './certificate-list.component';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateType } from '../model/certificate-type';
import { CertificateStatus } from '../model/certificate-status';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-certificates-page',
  standalone: true,
  imports: [CertificateList, CommonModule], 
  template: `
    <app-certificate-list [certificates]="certificates"></app-certificate-list>
  `,
})
export class CertificatesPageComponent {

  certificates: CertificateListItem[] = [
    {
      id: 1,
      commonName: 'api.myorg.rs',
      validFrom: '2025-01-01',
      validTo: '2026-01-01',
      type: CertificateType.END_ENTITY,
      status: CertificateStatus.VALID,
    },
    {
      id: 2,
      commonName: 'mail.myorg.rs',
      validFrom: '2025-01-01',
      validTo: '2026-01-01',
      type: CertificateType.END_ENTITY,
      status: CertificateStatus.REVOKED,
    },
    {
      id: 3,
      commonName: 'ca.myorg.rs',
      validFrom: '2024-06-01',
      validTo: '2027-06-01',
      type: CertificateType.INTERMEDIATE,
      status: CertificateStatus.VALID,
    },
    {
      id: 4,
      commonName: 'root.myorg.rs',
      validFrom: '2020-01-01',
      validTo: '2030-01-01',
      type: 'ROOT' as CertificateType,
      status: CertificateStatus.VALID,
      isExpanded: false,
      children: [
        {
          id: 3,
          commonName: 'ca.myorg.rs (Intermediate)',
          validFrom: '2024-06-01',
          validTo: '2027-06-01',
          type: 'INTERMEDIATE' as CertificateType,
          status: CertificateStatus.VALID,
          isExpanded: false,
          children: [
            {
              id: 1,
              commonName: 'api.myorg.rs (End-Entity)',
              validFrom: '2025-01-01',
              validTo: '2026-01-01',
              type: 'END ENTITY' as CertificateType,
              status: CertificateStatus.VALID,
            },{
              id: 1,
              commonName: 'api.myorg.rs (End-Entity)',
              validFrom: '2025-01-01',
              validTo: '2026-01-01',
              type: 'END ENTITY' as CertificateType,
              status: CertificateStatus.VALID,
            }
          ]
        },
        {
          id: 3,
          commonName: 'ca.myorg.rs (Intermediate)',
          validFrom: '2024-06-01',
          validTo: '2027-06-01',
          type: 'INTERMEDIATE' as CertificateType,
          status: 'VALID' as any,
          isExpanded: false,
          children: [
            {
              id: 1,
              commonName: 'api.myorg.rs (End-Entity)',
              validFrom: '2025-01-01',
              validTo: '2026-01-01',
              type: 'END ENTITY' as CertificateType,
              status: 'VALID' as any,
            },{
              id: 1,
              commonName: 'api.myorg.rs (End-Entity)',
              validFrom: '2025-01-01',
              validTo: '2026-01-01',
              type: 'END ENTITY' as CertificateType,
              status: 'VALID' as any,
            }
          ]
        }
      ]
    }
  ];
}