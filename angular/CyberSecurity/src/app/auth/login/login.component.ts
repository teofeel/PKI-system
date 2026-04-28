import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { CommonModule } from '@angular/common';
import { LoginData } from '../../model/LoginData';

@Component({
  selector: 'app-login.component',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  errormsg: string | null = null;


  formData = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)])
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
    }
  }

  login(){
    if (this.formData.invalid){
      this.detectErrors()
      return
    }
    
    const formValue = this.formData.value
    const loginData: LoginData = {
      email: formValue.email!,
      password: formValue.password!
    }

    this.authService.login(loginData).subscribe({
      next: (response) => {
      const token = response.body?.accessToken;
      if (token) {
        localStorage.setItem('jwt', token);
        this.router.navigate(['/']);
      }
    },
      error: (err) => {
        if (err.status === 401) {
        this.errormsg = 'Invalid email or password.';
      } else if (err.status === 403) {
        this.errormsg = 'Account not verified.';
      } else {
        this.errormsg = 'An error occurred during login.';
      }
        this.cdr.detectChanges();
      }
    });
  }

}
