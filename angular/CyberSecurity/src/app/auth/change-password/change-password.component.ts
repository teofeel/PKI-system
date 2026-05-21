import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ReactiveFormsModule } from '@angular/forms';
import { CryptoService } from '../../utils/crypto-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  router: Router = inject(Router);
  dialog: MatDialog = inject(MatDialog);

  errormsg: string | null = null;

  challengeDTO: any = { secret: null };

  selectedFile: File | null = null;
  selectedFileName: string = '';

  formData = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  constructor(
    private authService: AuthService,
    private cryptoService: CryptoService,
    private cdr: ChangeDetectorRef,
  ) {}

  detectErrors() {
    this.errormsg = null;
    const formValue = this.formData.value;
    if (!formValue.email) {
      this.errormsg = 'Email is required!';
    }
  }

  getChallenge() {
    if (this.formData.invalid) {
      this.detectErrors();
      return;
    }
    this.errormsg = '';
    this.authService.getChallenge(this.formData.value.email!).subscribe({
      next: (res) => {
        this.challengeDTO = res.body;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errormsg = 'This account does not exist!';
        this.cdr.detectChanges();
      },
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.selectedFileName = file.name;
      this.cdr.detectChanges();
    }
  }

  async onUpload() {
    if (!this.selectedFile || !this.challengeDTO.secret) return;

    try {
      const pemContent = await this.selectedFile.text();
      const privateKey = await this.cryptoService.importPrivateKey(pemContent);

      const decrypted = await this.cryptoService.decryptMessage(
        this.challengeDTO.secret,
        privateKey,
      );

      this.authService.changePasswordRequest(this.formData.value.email!, decrypted).subscribe({
        next: (res: any) => {
          const token = res.body.token;
          this.router.navigate(['/set-password', token]);
        },
        error: () => {
          this.errormsg = 'Verification did not work!.';
        },
      });
    } catch (err) {
      this.errormsg = 'Invalid private key file!';
      this.cdr.detectChanges();
    }
  }
}
