export interface RegistrationData {
  name: string;
  lastname: string;
  email: string;
  password: string;
  organization: string;
  accountType: AccountType;
  publicKey: string;
}

export interface Organization {
  name: string;
}

export enum AccountType {
  ADMIN,
  CA_USER,
  USER,
}
