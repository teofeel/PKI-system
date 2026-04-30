import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
export class CertificatesPage implements OnInit {
  certificates: CertificateListItem[] = [];
  isLoading = true;

  constructor(
    private certService: CertificateService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.isLoading = true;
    const role = this.authService.getRole();

    if (role === 'ADMIN') {
      this.certService.getAdminCertificates().subscribe({
        next: (certs) => {
          this.certificates = certs ? [...certs] : [];
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
        },
      });
    } else if (role === 'USER') {
      this.fetchCertificatesByAccount();
    } else {
      this.isLoading = false;
    }
  }

  private fetchCertificatesByAccount() {
    const accountId = this.authService.id();
    if (!accountId) {
      this.isLoading = false;
      return;
    }

    this.certService.fetchCertificatesByAccount(accountId).subscribe({
      next: (certificates) => {
        this.certificates = [...certificates];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }
}
