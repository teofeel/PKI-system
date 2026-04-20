import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegistrationData } from '../model/RegistrationData';
import { AuthService } from '../service/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css',
})
export class RegistrationComponent {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  errormsg: string | null = null;

  formData = new FormGroup({
    name: new FormControl('', Validators.required),
    lastname: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    confirmPassword: new FormControl('', Validators.required),
    organization: new FormControl(''),
  });

  detectErrors() {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.email) {
      this.errormsg = 'Email is required!';
    } else if (this.formData.get('email')?.hasError('email')) {
      this.errormsg = 'Please enter a valid email address!';
    } else if (!formValue.password) {
      this.errormsg = 'Password is required!';
    } else if (this.formData.get('password')?.hasError('minLength')) {
      this.errormsg = 'Password must be at least 6 characters long!';
    } else if (!formValue.confirmPassword) {
      this.errormsg = 'Please confirm your password!';
    } else if (formValue.password !== formValue.confirmPassword) {
      this.errormsg = 'Passwords do not match!';
    } else if (!formValue.name) {
      this.errormsg = 'Name is required!';
    } else if (!formValue.lastname) {
      this.errormsg = 'Last name is required!';
    }
  }

  register() {
    if (this.formData.invalid) {
      this.detectErrors();
      return;
    }

    const formValue = this.formData.value;

    const registrationData: RegistrationData = {
      email: formValue.email!,
      password: formValue.password!,
      name: formValue.name!,
      lastname: formValue.lastname!,
      organization: formValue.organization ?? '',
    };

    this.authService.register(registrationData).subscribe({
      error: () => {
        this.errormsg = 'Account with this email address already exists!';
        this.cdr.detectChanges();
      },
    });
  }
}
