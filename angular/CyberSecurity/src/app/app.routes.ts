import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegistrationComponent } from './auth/registration/registration.component';
import { VerifyComponent } from './auth/verify/verify.component';
import { SetPasswordComponent } from './auth/set-password/set-password.component';
import { CertificateFormComponent } from './certificates/certificate-form/certificate-form.component';
import { CreateOrganizationComponent } from './organization/create-organization/create-organization.component';
import { CertificatesPage } from './certificates/certificates-page/certificates-page';

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
    component: CreateOrganizationComponent
  },
  {
    path: 'certification-page',
    component: CertificatesPage
  }
];
