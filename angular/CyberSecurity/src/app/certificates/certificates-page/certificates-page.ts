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
export class CertificatesPage implements OnInit{
  certificates: CertificateListItem[] = [];

  constructor(
    private readonly certService: CertificateService,
    private readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const role = this.authService.role();

    if (role === 'ADMIN') {
      this.certService.getAdminCertificates().subscribe((certs) => (this.certificates = certs));
    }else if(role === 'USER'){
      this.fetchCertificatesByAccount();
    }
  }


  private fetchCertificatesByAccount(){
    const accountId = this.authService.id();

    if (!accountId) return;

    this.certService.fetchCertificatesByAccount(accountId).subscribe(
      (certificates: CertificateListItem[]) => {
        this.certificates = [...certificates]
        this.cdr.detectChanges();
      }
    )
  }
}
