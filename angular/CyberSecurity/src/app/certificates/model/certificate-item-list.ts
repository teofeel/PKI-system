import { CertificateStatus } from "./certificate-status";
import { CertificateType } from "./certificate-type";

export interface CertificateListItem {
  id: number;
  commonName: string;
  validFrom: string;
  validUntil: string;
  type: CertificateType;
  status: CertificateStatus;
  children?: CertificateListItem[];
  isExpanded?: boolean;
  isLoading?: boolean;
}