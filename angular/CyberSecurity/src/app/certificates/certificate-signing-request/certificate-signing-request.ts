import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'; 

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

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.initForms();
  }

  initForms() {
    this.autoGenForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      organization: ['', Validators.required],
      organizationUnit: [''],
      country: [''],
      certType: ['END_ENTITY', Validators.required],
      validFrom: ['', Validators.required],
      validTo: ['', Validators.required],
      parentCA: ['']
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

