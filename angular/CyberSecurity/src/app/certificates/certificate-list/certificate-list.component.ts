import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateType } from '../model/certificate-type';
import { Router, RouterModule } from '@angular/router';
import { CertificateService } from '../service/certificate-service';

@Component({
  selector: 'app-certificate-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './certificate-list.component.html',
  styleUrls: ['./certificate-list.component.css'],
})
export class CertificateList {
  @Input() certificates: CertificateListItem[] = [];

  constructor(
    private readonly router: Router,
    private readonly certService: CertificateService,
  ) {}

  getInitials(type: CertificateType): string {
    const map: Record<CertificateType, string> = {
      'ROOT':         'RCA',
      'INTERMEDIATE': 'ICA',
      'END_ENTITY':   'EE',
    };
    return map[type];
  }

  canExpand(type: CertificateType): boolean {
    return type === 'ROOT' || type === 'INTERMEDIATE';
  }

  goToDetails(certId: string | number): void {
    console.log('Navigating:', certId);
    this.router.navigate(['/certification', certId]);
  }

  onExpand(cert: CertificateListItem): void {
    cert.isExpanded = !cert.isExpanded;
    console.log(`Expanding certificate tree: ${cert.commonName}`);

    if (cert.isExpanded && !cert.children) {
      cert.isLoading = true;
      this.certService.getChildren(cert.id).subscribe((children) => {
        cert.children = children;
        cert.isLoading = false;
      });
    }
  }
}
