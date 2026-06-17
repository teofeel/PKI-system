import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegistrationComponent } from './auth/registration/registration.component';
import { VerifyComponent } from './auth/verify/verify.component';
import { SetPasswordComponent } from './auth/set-password/set-password.component';
import { CertificateFormComponent } from './certificates/certificate-form/certificate-form.component';
import { CreateOrganizationComponent } from './organization/create-organization/create-organization.component';
import { CertificatesPage } from './certificates/certificates-page/certificates-page';
import { Profile } from './profile/profile';
import { CertificateSigningRequest } from './certificates/certificate-signing-request/certificate-signing-request';
import { ChangePasswordComponent } from './auth/change-password/change-password.component';
import { CaApprovalPage } from './certificates/ca-approval-page/ca-approval-page';


export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegistrationComponent,
  },
  {
    path: 'verify/:token',
    component: VerifyComponent,
  },
  {
    path: 'set-password/:token',
    component: SetPasswordComponent,
  },
  {
    path: 'generate-certificate',
    component: CertificateFormComponent,
  },
  {
    path: 'create-organization',
    component: CreateOrganizationComponent,
  },
  {
    path: 'certification-page',
    component: CertificatesPage,
  },
  {
    path: 'profile',
    component: Profile
  },
  {
    path: 'csr',
    component: CertificateSigningRequest
  },
  {
    path: 'change-password',
    component: ChangePasswordComponent,
  },
  {
    path: 'ca-approval',
    component: CaApprovalPage,
  },
];
