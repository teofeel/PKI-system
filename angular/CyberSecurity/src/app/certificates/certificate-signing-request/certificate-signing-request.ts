import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'; 
import { OrganizationService } from '../../service/organization.service';
import { Organization } from '../../model/RegistrationData';
import { CertificateResponse } from '../model/certificate-response';
import { CertificateService } from '../service/certificate-service';
import { OrganizationDropdownDTO } from '../model/organization-dropdown-item';
import { CertificateDropdownListDTO } from '../model/certificate-dropdown-item';

type WorkflowState = 'SELECTION' | 'CSR_UPLOAD' | 'AUTO_GENERATE' | 'SUCCESS';
type GenMethod = 'AUTO' | 'UPLOAD' | null;

@Component({
  selector: 'certificate-signing-request',
  standalone: true, 
  imports: [CommonModule, ReactiveFormsModule], 
  templateUrl: './certificate-signing-request.html',
  styleUrls: ['./certificate-signing-request.css']
})
export class CertificateSigningRequest implements OnInit {
  currentState: WorkflowState = 'SELECTION';
  autoGenForm!: FormGroup;
  csrForm!: FormGroup;
  
  certTypes = ['ROOT', 'INTERMEDIATE', 'END_ENTITY'];
  showCertTypeDropdown = false;
  
  uploadedCsrFile: File | null = null;
  isDragging = false;

  generationMethod: GenMethod = null;
  organizations: OrganizationDropdownDTO[] = [];
  selectOrganization: OrganizationDropdownDTO | null = null;

  parentCAs: CertificateDropdownListDTO[] = [];


  constructor(
    private fb: FormBuilder, 
    private readonly organizationService: OrganizationService,
    private readonly certificateService: CertificateService
  ) {}

  ngOnInit() {
    this.initForms();
    this.prepareOrg();
  }

  prepareOrg() {
    this.organizationService.getOrganizations().subscribe((organizations) => {
      this.organizations = organizations;
    });
  }

  prepareParentCA(orgID: number){
    this.certificateService.getAllOrganizationCA(orgID).subscribe({
      next:(res) => {
        console.log(res);
        this.parentCAs = res;
      }
    })
  }

  onOrganizationSelected(){
    const currentForm = this.currentState === 'AUTO_GENERATE' ? this.autoGenForm : this.csrForm;
    const selectedId = currentForm.get('organization')?.value;

    if (!selectedId) {
      this.selectOrganization = null;
      this.parentCAs = [];
      return;
    }

    const orgId = Number(selectedId);

    const matchedOrg = this.organizations.find(org => org.id === orgId);
    if (matchedOrg) {
      this.selectOrganization = matchedOrg;
      
      if (this.currentState === 'AUTO_GENERATE') {
        this.autoGenForm.get('organizationUnit')?.setValue(''); 
      }
    }

    this.autoGenForm.get('parentCA')?.setValue('');
    this.csrForm.get('parentCA')?.setValue('');

    this.prepareParentCA(orgId);
  }

  initForms() {
    this.autoGenForm = this.fb.group({
      givenName: ['', Validators.required],
      surname: ['', Validators.required],
      commonName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      organization: ['', Validators.required],
      organizationUnit: ['', Validators.required],
      country: ['', [Validators.required, Validators.maxLength(2)]],
      parentCAId: [''], 
      validFrom: ['', Validators.required],
      validTo: ['', Validators.required],
      exportFormat: ['pkcs12', Validators.required], 
      keystorePassword: ['', Validators.required]
    });

    this.csrForm = this.fb.group({
      parentCA: ['', Validators.required],
      duration: ['30', Validators.required]
    });
  }

  setState(state: WorkflowState) {
    this.currentState = state;
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  handleFile(file: File) {
    if (file.name.endsWith('.pem')) {
      this.uploadedCsrFile = file;
    } else {
      alert('Invalid file type. Please upload a .pem file.');
    }
  }

  submitAutoGenerate() {
    if (this.autoGenForm.valid) {
      this.setState('SUCCESS');
    }
  }

  submitCsr() {
    if (this.csrForm.valid && this.uploadedCsrFile) {
      this.setState('SUCCESS');
    }
  }

  downloadKeyedBundle(format: 'pkcs12' | 'jks') {
    console.log(`API Target: Fetching active container map in ${format.toUpperCase()} structural binary array.`);
  }

  downloadCertificate(extension: 'pem' | 'cer') {
    console.log(`API Target: Downloading public X509 text blob inside standard .${extension} container layout.`);
  }


  resetWorkflow() {
      this.autoGenForm.reset({
      name: '',
      email: '',
      organization: '',
      organizationUnit: '',
      country: '',
      parentCA: '', 
      certType: 'END_ENTITY' 
    });

    this.csrForm.reset({
      parentCA: '',
      duration: '30' 
    });

    this.autoGenForm.markAsPristine();
    this.autoGenForm.markAsUntouched();
    this.autoGenForm.updateValueAndValidity();

    this.csrForm.markAsPristine();
    this.csrForm.markAsUntouched();
    this.csrForm.updateValueAndValidity();


    this.uploadedCsrFile = null;
    this.generationMethod = null;
    

    this.currentState = 'SELECTION';
  }
}

