import { Routes } from '@angular/router';
import { RegistrationComponent } from './registration/registration.component';
import { VerifyComponent } from './verify/verify.component';

export const routes: Routes = [
  {
    path: 'register',
    component: RegistrationComponent,
  },
  {
    path: 'verify/:token',
    component: VerifyComponent,
  },
];
