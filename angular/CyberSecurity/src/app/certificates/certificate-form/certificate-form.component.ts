import { Component } from '@angular/core';
import { environment } from '../../environments/environment';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Organization } from '../../model/RegistrationData';
import { CertificateResponse } from '../model/certificate-response';
import { CertificateType } from '../model/certificate-type';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../model/UserReponse';
import { OrganizationService } from '../../service/organization.service';

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
  owners: UserResponse[] = [];
  filteredOwners: UserResponse[] = [];
  parentCAs: CertificateResponse[] = [];
  certTypes: CertificateType[] = [];

  showOrgDropdown = false;
  showOwnerDropdown = false;
  showCertTypeDropdown = false;
  showParentDropdown = false;

  constructor(
    private userService: UserService,
    private organizationService: OrganizationService,
  ) {}

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
    } else if (!formValue.givenName) {
      this.errormsg = 'Given name is required!';
    } else if (!formValue.organization) {
      this.errormsg = 'Organization is required!';
    } else if (!formValue.organizationUnit) {
      this.errormsg = 'Organization unit is required!';
    } else if (!formValue.country) {
      this.errormsg = 'Country is required!';
    } else if (!formValue.owner) {
      this.errormsg = 'Owner is required!';
    } else if (!formValue.validFrom) {
      this.errormsg = 'Start Date is required!';
    } else if (!formValue.validTo) {
      this.errormsg = 'End Date is required!';
    } else if (!formValue.certType) {
      this.errormsg = 'Cert Type is required!';
    } else if (!formValue.parentCA) {
      this.errormsg = 'Parent CA is required!';
    }
  }

  ngOnInit() {
    this.certTypes = Object.values(CertificateType);
    this.prepareOrg();
    this.prepareUsers();
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

  hideOrgDropdown() {
    setTimeout(() => (this.showOrgDropdown = false));
  }

  prepareUsers() {
    this.userService.getUsers().subscribe((owners) => {
      this.owners = owners;
      this.filteredOwners = [...this.filteredOwners];
    });
  }

  filterOwners(event: Event) {
    const val = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredOwners = this.owners.filter((o) => o.name.toLowerCase().includes(val));
    this.showOwnerDropdown = true;
  }

  selectOwner(owner: UserResponse) {
    this.formData.get('owner')?.setValue(owner.name + ' ' + owner.lastname);
    this.showOwnerDropdown = false;
  }

  hideOwnerDropdown() {
    setTimeout(() => (this.showOwnerDropdown = false));
  }

  selectParentCA(parentCA: CertificateResponse) {
    this.formData.get('organization')?.setValue(parentCA.commonName);
    this.showParentDropdown = false;
  }

  hideParentDropdown() {
    setTimeout(() => (this.showParentDropdown = false));
  }

  selectCertType(certType: CertificateType) {
    this.formData.get('certType')?.setValue(certType);
    this.showCertTypeDropdown = false;
  }

  hideCertTypeDropdown() {
    setTimeout(() => (this.showCertTypeDropdown = false));
  }

  issue() {
    console.log(this.formData.value);
  }
}
