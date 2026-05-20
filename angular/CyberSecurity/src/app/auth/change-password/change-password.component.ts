import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';
import { LoginData } from '../../model/LoginData';
import { MatDialog } from '@angular/material/dialog';
import { SimpleMessageDialogComponent } from '../../utils/simple-message-dialog/simple-message-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  dialog: MatDialog = inject(MatDialog);

  errormsg: string | null = null;

  formData = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  detectErrors() {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.email) {
      this.errormsg = 'Email is required!';
    }
  }

  changePasswordRequest() {
    if (this.formData.invalid) {
      this.detectErrors();
      return;
    }

    const formValue = this.formData.value;
    const request = {
      email: formValue.email!,
    };

    this.authService.changePasswordRequest(request).subscribe({
      next: () => {
        this.dialog.open(SimpleMessageDialogComponent, {
          width: '300px',
          data: { message: 'We sent you an email. Check your e-mail to change your password!' },
        });
      },
      error: (err) => {
        if (err.status === 401) {
          this.errormsg = 'Invalid email.';
        } else if (err.status === 403) {
          this.errormsg = 'Account not verified.';
        } else {
          this.errormsg = 'An error occurred during request.';
        }
        this.cdr.detectChanges();
      },
    });
  }
}
