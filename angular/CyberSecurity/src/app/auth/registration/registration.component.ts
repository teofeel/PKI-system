import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountType, Organization, RegistrationData } from '../../model/RegistrationData';
import { AuthService } from '../../service/auth.service';
import { CommonModule } from '@angular/common';
import { OrganizationService } from '../../service/organization.service';
import { zxcvbn } from '@zxcvbn-ts/core';

@Component({
  selector: 'app-registration',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css',
})
export class RegistrationComponent implements OnInit {
  authService: AuthService = inject(AuthService);
  router: Router = inject(Router);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  organizations: Organization[] = [];
  filteredOrgs: Organization[] = [];
  showOrgDropdown = false;

  errormsg: string | null = null;

  strengthScore: number = 0;
  feedback: string = '';

  role: string | null;

  constructor(private organizationService: OrganizationService) {
    this.role = this.authService.role();
  }

  ngOnInit(): void {
    this.prepareOrg();

    if (this.role === 'ADMIN') {
      this.formData.get('password')?.clearValidators();
      this.formData.get('password')?.updateValueAndValidity();
      this.formData.get('confirmPassword')?.clearValidators();
      this.formData.get('confirmPassword')?.updateValueAndValidity();
    }
  }

  formData = new FormGroup({
    name: new FormControl('', Validators.required),
    lastname: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', Validators.required),
    organization: new FormControl('', Validators.required),
  });

  detectErrors() {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.email) {
      this.errormsg = 'Email is required!';
    } else if (this.formData.get('email')?.hasError('email')) {
      this.errormsg = 'Please enter a valid email address!';
    } else if (!formValue.password && this.role !== 'ADMIN') {
      this.errormsg = 'Password is required!';
    } else if (this.formData.get('password')?.hasError('minLength')) {
      this.errormsg = 'Password must be at least 6 characters long!';
    } else if (!formValue.confirmPassword && this.role !== 'ADMIN') {
      this.errormsg = 'Please confirm your password!';
    } else if (formValue.password !== formValue.confirmPassword) {
      this.errormsg = 'Passwords do not match!';
    } else if (!formValue.name) {
      this.errormsg = 'Name is required!';
    } else if (!formValue.lastname) {
      this.errormsg = 'Last name is required!';
    } else if (!formValue.organization) {
      this.errormsg = 'Organization is required!';
    }
  }

  checkPassword() {
    const result = zxcvbn(this.formData.value.password!);

    this.strengthScore = result.score;

    if (result.feedback.warning) {
      this.feedback = result.feedback.warning;
    } else if (result.feedback.suggestions.length) {
      this.feedback = result.feedback.suggestions.join(', ');
    } else {
      this.feedback = '';
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
      organization: formValue.organization!,
      accountType: AccountType.USER,
    };

    if (this.authService.isLoggedIn() == false) {
      registrationData.accountType = AccountType.USER;
      this.authService.register(registrationData).subscribe({
        error: () => {
          this.errormsg = 'Account with this email address already exists!';
          this.cdr.detectChanges();
        },
      });
    } else if (this.authService.role() == 'ADMIN') {
      registrationData.accountType = AccountType.CA_USER;
      this.authService.registerCAUser(registrationData).subscribe({
        error: () => {
          this.errormsg = 'Account with this email address already exists!';
          this.cdr.detectChanges();
        },
      });
    }
  }

  prepareOrg() {
    this.organizationService.getOrganizations().subscribe((organizations) => {
      this.organizations = organizations;
      this.filteredOrgs = [...this.organizations];
    });
  }

  filterOrgs(event: Event) {
    const val = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredOrgs = this.organizations.filter((o) => o.name.toLowerCase().includes(val));
    this.showOrgDropdown = true;
  }

  selectOrg(org: Organization) {
    this.formData.get('organization')?.setValue(org.name);
    this.showOrgDropdown = false;
  }

  hideDropdown() {
    setTimeout(() => (this.showOrgDropdown = false));
  }
}
