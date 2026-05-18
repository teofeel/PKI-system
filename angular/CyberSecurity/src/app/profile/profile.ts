import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GetAccountDetailsDTO } from '../model/GetAccountDetailsDTO';
import { UserService } from '../service/user.service';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MfaService } from '../service/mfa.service';
import { MfaQrCode } from '../model/MfaQrCode';
import { MfaCode } from '../model/MfaCode';
import { UpdateUserDTO } from '../model/UpdateUserDTO';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit{
  using2FA = false;
  errormsg: string | null = null;
  user: GetAccountDetailsDTO | null = null;
  profileForm!: FormGroup;

  showMfaSetup = false;
  qrCodeImage: string | null = null;
  mfaVerificationCode = '';
  mfaError: string | null = null;

  constructor (
    private readonly userService: UserService, 
    private readonly fb: FormBuilder, 
    private readonly mfaService: MfaService,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.createForm();
  }

  ngOnInit(): void{
    this.loadUserAccount();
  }

  loadUserAccount(): void{
    this.userService.getUser().subscribe({
      next: (data: GetAccountDetailsDTO) => {
        this.user = data;
        this.using2FA = data.using2FA;

        this.profileForm.patchValue({
          name: data.user.name,
          lastname: data.user.lastname,
          email: data.email,
          organization: data.user.organization
        });

        this.cdr.detectChanges();
      },
      error: () => {
        this.errormsg = 'Something went wrong fetching your data';
        this.cdr.detectChanges();
      }
    });
  }

  createForm(): void {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      lastname: ['', Validators.required],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      organization: [{ value: '', disabled: true }] 
    });
  }

  toggle2FA(): void {
    if(!this.using2FA){
      this.mfaService.setupMfa().subscribe({
        next: (res: MfaQrCode) => {
          console.log(res)
          this.mfaError = null;
          this.mfaVerificationCode = '';

          this.qrCodeImage = res.qrCode;
          this.showMfaSetup = true; 

          this.cdr.markForCheck();
          this.cdr.detectChanges();
        },
        error: () => {
          this.errormsg = 'Could not initiate 2FA setup.';
          this.cdr.detectChanges();
        }
      });
    }else{
      this.mfaError = null;
      this.mfaVerificationCode = '';
      this.qrCodeImage = null;             
      this.showMfaSetup = true;            

      this.cdr.detectChanges();
    }
  }

  confirmMfa(): void {
    this.mfaError = null;

    if (this.mfaVerificationCode.length !== 6) {
      this.mfaError = 'Code must be exactly 6 digits.';
      return;
    }
    
    const mfaCodeDto: MfaCode = {
        code: this.mfaVerificationCode
    };
    

    if(!this.using2FA){
      this.mfaService.setupVerifyMfa(mfaCodeDto).subscribe({
        next: (res: any) => {
          
          this.using2FA = true;          
          this.showMfaSetup = false;     
          this.qrCodeImage = null;       
          this.mfaVerificationCode = ''; 
          this.mfaError = null;
          this.closeMfaModal();
          this.cdr.detectChanges();      
        },
        error: (err: any) => {
          this.mfaError = err.error?.message || err.error || 'Invalid code, please try again.';
          this.cdr.detectChanges();
        }
      });
    }else{
      this.mfaService.removeVerifyMfa(mfaCodeDto).subscribe({
          next: () => {
              this.using2FA = false;
              this.showMfaSetup = false;
              this.mfaVerificationCode = '';
              this.mfaError = null;
              this.closeMfaModal();
              this.cdr.detectChanges();
          },
          error: (err: any) => {
              this.mfaError = err.error?.message || err.error || 'Invalid code, please try again.';
              this.cdr.detectChanges();
          }
      });
    }
  }


  closeMfaModal(): void {
    this.showMfaSetup = false;
    this.qrCodeImage = null;
    this.mfaVerificationCode = '';
    this.mfaError = null;
    this.cdr.detectChanges();
  }

  saveProfile(): void {
    this.errormsg = null;

    if (this.profileForm.invalid) {
    this.errormsg = 'Please fill in all required fields correctly.';
    return;
  }

  const updateDto: UpdateUserDTO = {
    firstname: this.profileForm.value.name,
    lastname: this.profileForm.value.lastname
  };


  this.userService.updateUser(updateDto).subscribe({
    next: (updatedData) => {
      if (this.user) {
        this.user.user.name = updateDto.firstname;
        this.user.user.lastname = updateDto.lastname;
      }

      alert('Profile updated successfully!'); 
      this.cdr.detectChanges();
    },
    error: (err) => {
      this.errormsg = err.error?.message || 'Failed to update profile information.';
      this.cdr.detectChanges();
    }
  });
  }
}
