import { ChangeDetectorRef, Component } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { zxcvbn } from '@zxcvbn-ts/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { SimpleMessageDialogComponent } from '../../utils/simple-message-dialog/simple-message-dialog.component';

@Component({
  selector: 'app-set-password',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './set-password.component.html',
  styleUrl: './set-password.component.css',
})
export class SetPasswordComponent {
  validToken: boolean | null = null;
  passwordForm = new FormGroup({
    password: new FormControl('', Validators.minLength(8)),
    confirm: new FormControl('', Validators.minLength(8)),
  });
  errormsg: string = '';
  showError: boolean = false;

  strengthScore: number = 0;
  feedback: string = '';

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.authService.checkSetPassword(this.route.snapshot.paramMap.get('token')!).subscribe({
      next: () => {
        this.validToken = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.validToken = false;
        this.cdr.detectChanges();
      },
    });
  }

  checkPassword() {
    const result = zxcvbn(this.passwordForm.value.password!);

    this.strengthScore = result.score;

    if (result.feedback.warning) {
      this.feedback = result.feedback.warning;
    } else if (result.feedback.suggestions.length) {
      this.feedback = result.feedback.suggestions.join(', ');
    } else {
      this.feedback = '';
    }
  }

  setPassword() {
    if (!this.passwordForm.valid) {
      this.errormsg = 'Password needs to be at least 8 characters long!';
      this.showError = true;
    } else if (this.passwordForm.value.password != this.passwordForm.value.confirm) {
      this.errormsg = "Confirm password doesn't match password";
      this.showError = true;
    } else {
      this.authService
        .setPassword(this.route.snapshot.paramMap.get('token')!, this.passwordForm.value.password!)
        .subscribe({
          next: () => {
            this.dialog.open(SimpleMessageDialogComponent, {
              width: '300px',
              data: { message: 'You changed password successfully!' },
            });
          },
          error: () => {
            this.errormsg = 'Something went wrong!';
            this.showError = true;
            this.cdr.detectChanges();
          },
        });
    }
  }
}
