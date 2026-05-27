export interface AutoGenerateRequestDTO {
  commonName: string;
  givenName: string;
  surname: string;
  email: string;
  organization: string;
  organizationUnit: string;
  country: string;
  userId: number | null;       
  validFrom: string;    
  validTo: string;
  parentCAId: string;   
  exportFormat: string; 
  keystorePassword: string;
  alias?: string;
}