import { SubjectIssuerDTO } from './subject-issuer';

export interface CertificateDetailsDTO {
  id: string;
  issuer: SubjectIssuerDTO;
  subject: SubjectIssuerDTO;
  validFrom: Date;
  validTo: Date;
  certificateKey: string;
  publicKey: string;
}
