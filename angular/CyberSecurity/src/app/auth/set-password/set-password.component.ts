import { ChangeDetectorRef, Component } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-set-password',
  imports: [ReactiveFormsModule],
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

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
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
          next: () => {},
          error: () => {
            this.errormsg = 'Something went wrong!';
            this.showError = true;
            this.cdr.detectChanges();
          },
        });
    }
  }
}
