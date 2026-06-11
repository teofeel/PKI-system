import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CertificateService } from '../service/certificate-service';
import { CertificateApprovalItem } from '../model/certificate-item-list';
import { CertificateApprovalStatus } from '../model/certificate-status';

type TabType = 'pending' | 'rejected';

@Component({
  selector: 'app-ca-approval-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ca-approval-page.html',
  styleUrls: ['./ca-approval-page.css'],
})
export class CaApprovalPage implements OnInit {
  pendingCertificates: CertificateApprovalItem[] = [];
  rejectedCertificates: CertificateApprovalItem[] = [];

  activeTab: TabType = 'pending';

  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  showConfirmDialog = false;
  confirmAction: 'approve' | 'reject' = 'approve';
  confirmCert: CertificateApprovalItem | null = null;
  rejectReason = '';
  actionInProgress = false;

  constructor(
    private certService: CertificateService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.loadPending();
  }

  setTab(tab: TabType) {
    this.activeTab = tab;
    this.errorMessage = null;
    this.successMessage = null;

    if (tab === 'pending') {
      this.loadPending();
    } else {
      this.loadRejected();
    }
  }

  loadPending() {
    this.isLoading = true;
    this.errorMessage = null;

    this.certService.getPendingCertificates().subscribe({
      next: (certs) => {
        this.pendingCertificates = certs || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load certs';
        this.isLoading = false;


        this.cdr.detectChanges();
      },
    });
  }




  loadRejected() {
    this.isLoading = true;
    this.errorMessage = null;

    this.certService.getRejectedCertificates().subscribe({
      next: (certs) => {
        this.rejectedCertificates = certs || [];
        this.isLoading = false;

        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load rejected certificates';
        this.isLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  openApproveDialog(cert: CertificateApprovalItem) {
    this.confirmAction = 'approve';
    this.confirmCert = cert;

    this.rejectReason = '';
    this.showConfirmDialog = true;
  }

  openRejectDialog(cert: CertificateApprovalItem) {
    this.confirmAction = 'reject';
    this.confirmCert = cert;

    this.rejectReason = '';
    this.showConfirmDialog = true;
  }

  cancelDialog() {
    this.showConfirmDialog = false;
    this.confirmCert = null;

    this.rejectReason = '';
    this.actionInProgress = false;
  }

  confirmActionHandler() {
    if (!this.confirmCert) return;

    this.actionInProgress = true;
    this.errorMessage = null;
    this.successMessage = null;

    if (this.confirmAction === 'approve') {
      this.certService.approveCertificate(this.confirmCert.id).subscribe({
        next: (result) => {
          this.showConfirmDialog = false;
          this.confirmCert = null;
          this.actionInProgress = false;
          this.successMessage = `Certificate "${result.commonName}" has been approved`;

          this.loadPending();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.actionInProgress = false;
          this.errorMessage = 'Failed to approve certificate. It may have been processed already';

          this.cdr.detectChanges();
        },



      });

    } else {
      this.certService
        .rejectCertificate(this.confirmCert.id, this.rejectReason.trim() || undefined)
        .subscribe({
          next: (result) => {
            this.showConfirmDialog = false;
            this.confirmCert = null;
            this.actionInProgress = false;
            this.successMessage = `Certificate "${result.commonName}" has been rejected`;

            this.loadPending();
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.actionInProgress = false;
            this.errorMessage = 'Failed to reject certificate. It may have been processed already';
            this.cdr.detectChanges();
          },
        });
    }
  }

  getTypeInitials(type: string): string {
    const map: Record<string, string> = {
      ROOT: 'RCA',
      INTERMEDIATE: 'ICA',
      END_ENTITY: 'EE',
    };
    return map[type] || type;
  }

  getApprovalStatusClass(status: CertificateApprovalStatus): string {
    return status.toLowerCase();
  }
}
