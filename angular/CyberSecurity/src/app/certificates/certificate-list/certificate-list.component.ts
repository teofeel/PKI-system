import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { CertificateListItem } from '../model/certificate-item-list';
import { CertificateType } from '../model/certificate-type';
import { Router, RouterModule } from '@angular/router';
import { CertificateService } from '../service/certificate-service';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { SimpleMessageDialogComponent } from '../../utils/simple-message-dialog/simple-message-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-certificate-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './certificate-list.component.html',
  styleUrls: ['./certificate-list.component.css'],
})
export class CertificateList {
  @Input() certificates: CertificateListItem[] = [];
  @Input() isLoading: boolean = false;
  @Input() role: string | null = null;

  constructor(
    private readonly router: Router,
    private certService: CertificateService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog,
  ) {}

  getInitials(type: CertificateType): string {
    const map: Record<CertificateType, string> = {
      ROOT: 'RCA',
      INTERMEDIATE: 'ICA',
      END_ENTITY: 'EE',
    };
    return map[type];
  }

  canExpand(type: CertificateType): boolean {
    if (this.role === 'USER') return false;
    return type === 'ROOT' || type === 'INTERMEDIATE';
  }

  goToDetails(certId: string | number): void {
    console.log('Navigating:', certId);
    //this.router.navigate(['/certification', certId]);
  }

  onExpand(cert: CertificateListItem): void {
    cert.isExpanded = !cert.isExpanded;
    console.log(`Expanding certificate tree: ${cert.commonName}`);

    if (cert.isExpanded && !cert.children) {
      cert.isLoading = true;

      this.certService.getChildren(cert.id).subscribe({
        next: (children) => {
          cert.children = children;
          cert.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          cert.isLoading = false;
          this.cdr.detectChanges();
        },
      });
    } else {
      this.cdr.detectChanges();
    }
  }

  download(id: number, format: string, isUsingPk: boolean, passwordInput?: HTMLInputElement): void {
    let downloadObservable$: Observable<HttpResponse<Blob>>;

    if (isUsingPk) {
      const password = passwordInput?.value;
      if (!password || !password.trim()) {
        alert('Password cannot be empty.');
        return;
      }
      downloadObservable$ = this.certService.exportKeystore(id, password);
    } else {
      downloadObservable$ = this.certService.downloadCertificateNoPk(id, format);
    }

    this.certificates.forEach((c) => {
      c.showDownloadMenu = false;
      (c as any).showPasswordForm = false;
    });

    downloadObservable$.subscribe({
      next: (response: HttpResponse<Blob>) => {
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = `certificate.${format}`;

        if (contentDisposition) {
          const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;\n]*)/);
          if (utf8Match && utf8Match[1]) {
            filename = decodeURIComponent(utf8Match[1]);
          } else {
            const classicMatch = contentDisposition.match(/filename="?([^";\n]+)"?/);
            if (classicMatch && classicMatch[1]) {
              filename = classicMatch[1];
            }
          }
        }

        const blob = response.body;
        if (blob) {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          globalThis.URL.revokeObjectURL(url);
        }
      },
      error: (err) => {
        console.error('Download failed', err);
      },
    });
  }

  toggleDownloadMenu(cert: CertificateListItem, event: Event): void {
    event.stopPropagation();

    const wasOpen = cert.showDownloadMenu;

    this.certificates.forEach((c) => (c.showDownloadMenu = false));

    if (!wasOpen) {
      cert.showDownloadMenu = true;
    }
  }

  switchToPasswordForm(cert: CertificateListItem, event: Event): void {
    event.stopPropagation();
    (cert as any).showPasswordForm = true;
  }

  cancelPasswordForm(cert: CertificateListItem, event: Event): void {
    event.stopPropagation();
    (cert as any).showPasswordForm = false;
  }

  toggleRevokeForm(cert: CertificateListItem, event: Event): void {
    event.stopPropagation();
    cert.showRevokeForm = !cert.showRevokeForm;
    cert.revokeReason = '';
    cert.revokeNote = '';
  }

  cancelRevokeForm(cert: any, event: Event): void {
    event.stopPropagation();
    cert.showRevokeForm = false;
    cert.revokeReason = '';
    cert.revokeNote = '';
  }

  onRevokeReasonChange(cert: CertificateListItem): void {
    if (cert.revokeReason !== 'other') cert.revokeNote = '';
  }

  confirmRevoke(cert: CertificateListItem, event: Event): void {
    event.stopPropagation();
    this.certService.revoke(cert).subscribe({
      next: () => {
        this.dialog.open(SimpleMessageDialogComponent, {
          width: '300px',
          data: {
            message: 'Certificate successfully revoked',
          },
        });
      },
      error: (err) => {
        console.error(err);
      },
    });
    cert.showRevokeForm = false;
  }
}
