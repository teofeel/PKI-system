import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'; 
import { OrganizationService } from '../../service/organization.service';
import { CertificateService } from '../service/certificate-service';
import { OrganizationDropdownDTO } from '../model/organization-dropdown-item';
import { CertificateDropdownListDTO } from '../model/certificate-dropdown-item';
import { AutoGenerateRequestDTO } from '../model/auto-generate-request';
import { AuthService } from '../../service/auth.service';
import { HttpResponse } from '@angular/common/http';

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

  generatedCertId: number | null = null;


  constructor(
    private readonly fb: FormBuilder, 
    private readonly organizationService: OrganizationService,
    private readonly certificateService: CertificateService,
    private readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.initForms();
    this.prepareOrg();
    this.prepareParentCA();
  }

  prepareOrg() {
    this.organizationService.getOrganizations().subscribe((organizations) => {
      this.organizations = organizations;
    });
  }

  prepareParentCA(){
    this.certificateService.getAllCA().subscribe({
      next:(res) => {
        console.log(res);
        this.parentCAs = res;
      }
    })
  }

  onOrganizationSelected(){
    const currentForm = this.currentState === 'AUTO_GENERATE' ? this.autoGenForm : this.csrForm;
    const selectedName = currentForm.get('organization')?.value;

    if (!selectedName) {
      this.selectOrganization = null;
      this.parentCAs = [];
      return;
    }

    const orgName= selectedName;

    const matchedOrg = this.organizations.find(org => org.name === orgName);
    if (matchedOrg) {
      this.selectOrganization = matchedOrg;
      
      if (this.currentState === 'AUTO_GENERATE') {
        this.autoGenForm.get('organizationUnit')?.setValue(''); 
      }
    }
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
      validFrom: ['', Validators.required],
      validTo: ['', Validators.required]
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
      alert('Invalid file type. Please upload a .pem file');
    }
  }

  submitAutoGenerate() {
    if (this.autoGenForm.invalid) {
      this.autoGenForm.markAllAsTouched();
      return;
    }

    const formValue = this.autoGenForm.value;
    const requestData: AutoGenerateRequestDTO = {
      givenName: formValue.givenName,
      surname: formValue.surname,
      commonName: formValue.commonName,
      email: formValue.email,
      organization: formValue.organization,
      organizationUnit: formValue.organizationUnit,
      country: formValue.country,
      parentCAId: formValue.parentCAId ? formValue.parentCAId : null,
      validFrom: formValue.validFrom,
      validTo: formValue.validTo,
      exportFormat: formValue.exportFormat,
      keystorePassword: formValue.keystorePassword,
      userId: this.authService.getId()
    }

    this.certificateService.generateEECert(requestData).subscribe({
      next:(response) => {
        const blob = response.body;
        if(blob){
          const extension = formValue.exportFormat === 'pkcs12' ? 'p12' : 'jks';
          const filename = `${formValue.commonName}.${extension}`;

          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = filename;
          link.click();

          const certId = response.headers.get('X-Certificate-Id');
          console.log("Generisan ID:", certId);
          this.generatedCertId = Number(certId);
        
          window.URL.revokeObjectURL(url);
        }
        
        this.generationMethod = 'AUTO';
        this.setState('SUCCESS');
        this.cdr.detectChanges();
      },
      error: (err) => {
        alert('An error occurred while generating');
      }
    })
  }

  submitCsr() {
    if (this.csrForm.valid && !this.uploadedCsrFile) return;
    
    const data: any = {
      parentCAId: this.csrForm.get('parentCA')?.value,
      validFrom: this.csrForm.get('validFrom')?.value,
      validTo: this.csrForm.get('validTo')?.value
    };

    const formData = new FormData();
    formData.append('file', this.uploadedCsrFile!);
    formData.append('data', new Blob([JSON.stringify(data)], {
        type: 'application/json'
    }));

    this.certificateService.generateCsrEECert(formData).subscribe({
        next: (certID: number) => {
            this.generatedCertId = certID;
            this.generationMethod = 'UPLOAD';
            this.setState('SUCCESS');
            this.cdr.detectChanges();
        },
        error: (err) => {
          alert("Couldn't generate EE cert");
        }
    });
  }

  downloadCertificate(extension: 'pem' | 'cer') {
    console.log(this.generatedCertId)
    this.certificateService.downloadCertificateNoPk(this.generatedCertId!, extension)
        .subscribe({
          next: (response: HttpResponse<Blob>) => {
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = `certificate.${extension}`; 

            if (contentDisposition) {
              const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;\n]*)/);
              if (utf8Match && utf8Match[1]) {
                filename = decodeURIComponent(utf8Match[1]);
              } else {
                const classicMatch = contentDisposition.match(/filename="?([^";\n]+)"?/);
                if (classicMatch && classicMatch[1]) {
                  filename = classicMatch[1];
                }
              }
            }
            
            const blob = response.body;
            if (blob) {
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = filename;
              document.body.appendChild(a);
              a.click();
              document.body.removeChild(a);
              globalThis.URL.revokeObjectURL(url);
            }
          },
          error: (err) => {
            console.error('Download failed', err);
          }
        });

    console.log(`API Target: Downloading public X509 text blob inside standard .${extension} container layout`);
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

