import { CertificateType } from './certificate-type';

export interface CertificateRequest {
  commonName: string;
  surname: string;
  givenName: string;
  organization: string;
  organizationUnit: string;
  country: string;
  email: string;

  userId: number;

  validFrom: Date;
  validTo: Date;

  parentCAId?: string;
  certificateType: CertificateType;
}
