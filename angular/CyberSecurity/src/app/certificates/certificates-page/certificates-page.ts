import { Component } from '@angular/core';
import { CertificateType } from '../model/certificate-type';
import { CertificateStatus } from '../model/certificate-status';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateList } from '../certificate-list/certificate-list.component';
import { CommonModule } from '@angular/common';
import { CertificateService } from '../service/certificate-service';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-certificates-page',
  standalone: true,
  imports: [CertificateList, CommonModule],
  templateUrl: './certificates-page.html',
  styleUrl: './certificates-page.css',
})
export class CertificatesPage {
  certificates: CertificateListItem[] = [];

  constructor(
    private certService: CertificateService,
    private authService: AuthService,
  ) {}

  ngOnInit() {
    const role = this.authService.role();

    if (role === 'ADMIN') {
      this.certService.getAdminCertificates().subscribe((certs) => (this.certificates = certs));
    }
  }
}
