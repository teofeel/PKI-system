import { Routes } from '@angular/router';
import { RegistrationComponent } from './auth/registration/registration.component';
import { VerifyComponent } from './auth/verify/verify.component';
import { SetPasswordComponent } from './auth/set-password/set-password.component';

export const routes: Routes = [
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
];
