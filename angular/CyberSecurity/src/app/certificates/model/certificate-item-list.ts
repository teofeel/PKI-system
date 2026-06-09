import { CertificateApprovalStatus, CertificateStatus } from './certificate-status';
import { CertificateType } from './certificate-type';

export interface CertificateListItem {
  id: number;
  commonName: string;
  validFrom: string;
  validUntil: string;
  type: CertificateType;
  status: CertificateStatus;
  showDownloadMenu?: boolean;
  children?: CertificateListItem[];
  isExpanded?: boolean;
  isLoading?: boolean;
}

export interface CertificateApprovalItem {
  id: number;
  commonName: string;
  organizationName: string;
  validFrom: string;
  validUntil: string;
  type: CertificateType;
  status: string;
  approvalStatus: CertificateApprovalStatus;
  approvedBy: string | null;
  approvedAt: string | null;
  rejectionReason: string | null;
}