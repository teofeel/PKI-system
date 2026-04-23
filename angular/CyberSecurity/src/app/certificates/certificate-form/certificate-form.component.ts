import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Organization } from '../../model/RegistrationData';
import { CertificateResponse } from '../model/certificate-response';
import { CertificateType } from '../model/certificate-type';

@Component({
  selector: 'app-certificate-form',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './certificate-form.component.html',
  styleUrl: './certificate-form.component.css',
})
export class CertificateFormComponent {
  errormsg: string | null = '';
  organizations: Organization[] = [];
  filteredOrgs: Organization[] = [];
  owners: Organization[] = [];
  filteredOwners: Organization[] = [];
  parentCAs: CertificateResponse[] = [];
  certTypes: CertificateType[] = [];

  showOrgDropdown = false;
  showOwnerDropdown = false;
  showCertTypeDropdown = false;
  showParentDropdown = false;
  formData = new FormGroup({
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    givenName: new FormControl('', Validators.required),
    organization: new FormControl('', [Validators.required, Validators.minLength(8)]),
    organizationUnit: new FormControl('', Validators.required),
    country: new FormControl('', Validators.required),
    owner: new FormControl('', Validators.required),
    certType: new FormControl<CertificateType>(CertificateType.END_ENTITY, Validators.required),
    validFrom: new FormControl('', Validators.required),
    validTo: new FormControl('', Validators.required),
    parentCA: new FormControl('', Validators.required),
  });

  detectErrors() {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.email) {
      this.errormsg = 'Email is required!';
    } else if (this.formData.get('email')?.hasError('email')) {
      this.errormsg = 'Please enter a valid email address!';
    } else if (!formValue.name) {
      this.errormsg = 'Name is required!';
    } else if (!formValue.surname) {
      this.errormsg = 'Surname is required!';
    } else if (!formValue.organization) {
      this.errormsg = 'Organization is required!';
    }
  }

  ngOnInit() {
    this.certTypes = Object.values(CertificateType);
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

  hideOrgDropdown() {
    setTimeout(() => (this.showOrgDropdown = false));
  }

  selectOwner(org: Organization) {
    this.formData.get('organization')?.setValue(org.name);
    this.showOrgDropdown = false;
  }

  hideOwnerDropdown() {
    setTimeout(() => (this.showOrgDropdown = false));
  }

  selectParentCA(parentCA: CertificateResponse) {
    this.formData.get('organization')?.setValue(parentCA.CommonName);
    this.showOrgDropdown = false;
  }

  hideParentDropdown() {
    setTimeout(() => (this.showParentDropdown = false));
  }

  selectCertType(certType: CertificateType) {
    this.formData.get('certType')?.setValue(certType);
    this.showOrgDropdown = false;
  }

  hideCertTypeDropdown() {
    setTimeout(() => (this.showCertTypeDropdown = false));
  }

  issue() {}
}
