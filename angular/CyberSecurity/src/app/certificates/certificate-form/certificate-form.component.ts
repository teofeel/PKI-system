import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Organization } from '../../model/RegistrationData';
import { CertificateResponse } from '../model/certificate-response';
import { CertificateType } from '../model/certificate-type';
import { UserService } from '../../service/user.service';
import { UserResponse } from '../../model/UserReponse';
import { OrganizationService } from '../../service/organization.service';
import { CertificateRequest } from '../model/certificate-request';
import { CertificateService } from '../service/certificate-service';

@Component({
  selector: 'app-certificate-form',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './certificate-form.component.html',
  styleUrl: './certificate-form.component.css',
})
export class CertificateFormComponent implements OnInit {
  errormsg: string | null = '';

  organizations: Organization[] = [];
  filteredOrgs: Organization[] = [];
  owners: UserResponse[] = [];
  filteredOwners: UserResponse[] = [];
  parentCAs: CertificateResponse[] = [];
  certTypes: CertificateType[] = [];

  selectedOwner: UserResponse | null = null;
  selectedParentCA: CertificateResponse | null = null;

  showOrgDropdown = false;
  showOwnerDropdown = false;
  showCertTypeDropdown = false;
  showParentDropdown = false;

  constructor(
    private userService: UserService,
    private organizationService: OrganizationService,
    private certificateService: CertificateService,
    private cdr : ChangeDetectorRef
  ) {}

  formData = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    surname: new FormControl<string>('', Validators.required),
    email: new FormControl<string>('', [Validators.required, Validators.email]),
    givenName: new FormControl<string>('', Validators.required),
    organization: new FormControl<string>('', Validators.required),
    organizationUnit: new FormControl<string>('', Validators.required),
    country: new FormControl<string>('', Validators.required),
    ownerDisplay: new FormControl<string>('', Validators.required),
    certType: new FormControl<CertificateType>(CertificateType.END_ENTITY, Validators.required),
    validFrom: new FormControl<string>('', Validators.required),
    validTo: new FormControl<string>('', Validators.required),
    parentCADisplay: new FormControl<string>(''),
  });

  detectErrors(): boolean {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.name) {
      this.errormsg = 'Name is required!';
    } else if (!formValue.surname) {
      this.errormsg = 'Surname is required!';
    } else if (!formValue.givenName) {
      this.errormsg = 'Given name is required!';
    } else if (!formValue.email) {
      this.errormsg = 'Email is required!';
    } else if (this.formData.get('email')?.hasError('email')) {
      this.errormsg = 'Please enter a valid email address!';
    } else if (!formValue.organization) {
      this.errormsg = 'Organization is required!';
    } else if (!formValue.organizationUnit) {
      this.errormsg = 'Organization unit is required!';
    } else if (!formValue.country) {
      this.errormsg = 'Country is required!';
    } else if (!this.selectedOwner) {
      this.errormsg = 'Owner is required!';
    } else if (!formValue.validFrom) {
      this.errormsg = 'Start date is required!';
    } else if (!formValue.validTo) {
      this.errormsg = 'End date is required!';
    } else if (new Date(formValue.validFrom) >= new Date(formValue.validTo)) {
      this.errormsg = 'End date must be after start date!';
    } else if (new Date(formValue.validFrom) < new Date(Date.now())) {
      this.errormsg = 'Start date must be in the future!';
    } else if (new Date(formValue.validTo) < new Date(Date.now())) {
      this.errormsg = 'End date must be in the future!';
    } else if (!formValue.certType) {
      this.errormsg = 'Certificate type is required!';
    }

    return this.errormsg === null;
  }

  ngOnInit() {
    this.certTypes = Object.values(CertificateType);
    this.prepareOrg();
    this.prepareUsers();
    this.prepareParentCA();
  }

  prepareParentCA(){
    this.certificateService.getAllParentCA().subscribe((parentCAs) => {
      this.parentCAs = parentCAs;
    })
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
      this.filteredOwners = [...this.owners];
    });
  }

  filterOwners(event: Event) {
    const val = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredOwners = this.owners.filter((o) =>
      (o.name + ' ' + o.lastname).toLowerCase().includes(val),
    );
    this.showOwnerDropdown = true;
  }

  selectOwner(owner: UserResponse) {
    this.selectedOwner = owner;
    this.formData.get('ownerDisplay')?.setValue(owner.name + ' ' + owner.lastname);
    this.showOwnerDropdown = false;
  }

  hideOwnerDropdown() {
    setTimeout(() => (this.showOwnerDropdown = false));
  }

  selectParentCA(parentCA: CertificateResponse) {
    this.selectedParentCA = parentCA;
    this.formData.get('parentCADisplay')?.setValue(parentCA.commonName);
    this.showParentDropdown = false;
  }

  hideParentDropdown() {
    setTimeout(() => (this.showParentDropdown = false));
  }

  selectCertType(certType: CertificateType) {
    this.formData.get('certType')?.setValue(certType);
    if (certType === CertificateType.ROOT) {
      this.selectedParentCA = null;
      this.formData.get('parentCADisplay')?.setValue('');
    }
    this.showCertTypeDropdown = false;
    this.cdr.detectChanges();
  }

  

  hideCertTypeDropdown() {
    setTimeout(() => (this.showCertTypeDropdown = false));
  }

  issue() {
    const valid = this.detectErrors();
    if (!valid || this.formData.invalid || !this.selectedOwner) {
      return;
    }

    const formValue = this.formData.value;

    const certificateRequest: CertificateRequest = {
      email: formValue.email!,
      commonName: formValue.name!,
      surname: formValue.surname!,
      givenName: formValue.givenName!,
      organization: formValue.organization!,
      organizationUnit: formValue.organizationUnit!,
      country: formValue.country!,
      userId: this.selectedOwner.id,
      validFrom: new Date(formValue.validFrom!),
      validTo: new Date(formValue.validTo!),
      certificateType: formValue.certType!,
    };

    if (this.selectedParentCA != null) certificateRequest.parentCAId = this.selectedParentCA?.id;

    console.log(certificateRequest);

    this.certificateService.issue(certificateRequest).subscribe({
      next: (res) => {
        console.log(res)
      },
      error: (err) => {
        console.log(err)
        if (err.status == 400){
          this.errormsg = 'Some of the data entered is wrong'
        } else if (err.status === 500) {
          this.errormsg = 'There has been an internal server error'
        } else {
          this.errormsg = 'Failed to issue certificate!';
        }
      }
    });
    this.formData.reset()
    this.cdr.detectChanges()
  }
}
