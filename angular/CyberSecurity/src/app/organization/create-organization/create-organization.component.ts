import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { OrganizationService } from '../../service/organization.service';
import { Organization } from '../../model/RegistrationData';

@Component({
  selector: 'app-create-organization',
  imports: [ReactiveFormsModule],
  templateUrl: './create-organization.component.html',
  styleUrl: './create-organization.component.css',
})
export class CreateOrganizationComponent {
  constructor(private orgService: OrganizationService) {}

  formData = new FormGroup({
    name: new FormControl('', [Validators.required]),
  });
  create() {
    const org: Organization = {
      name: this.formData.value.name!,
    };
    this.orgService.saveOrganization(org).subscribe({
      next: () => {},
    });
  }
}
