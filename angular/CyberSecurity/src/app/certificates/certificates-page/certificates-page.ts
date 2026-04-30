import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CertificateType } from '../model/certificate-type';
import { CertificateStatus } from '../model/certificate-status';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateList } from '../certificate-list/certificate-list.component';
import { CommonModule } from '@angular/common';
import { JwtService } from '../../service/jwt.service';
import { CertificateService } from '../service/certificate-service';
@Component({
  selector: 'app-certificates-page',
  standalone: true,
  imports: [CertificateList, CommonModule], 
  templateUrl: './certificates-page.html',
  styleUrl: './certificates-page.css',
})
export class CertificatesPage implements OnInit {
    certificates: CertificateListItem[] = [];


    constructor(private certService: CertificateService, private jwtService: JwtService, private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
      this.fetchCertificatesByAccount();
    }

    private fetchCertificatesByAccount(){
      const accountId = this.jwtService.getJwtToken()?.id;

      if (!accountId) return;

      this.certService.fetchCertificatesByAccount(accountId).subscribe(
        (certificates: CertificateListItem[]) => {
          this.certificates = [...certificates]
          this.cdr.detectChanges();
        }
      )
    }
}
