import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { CryptoService } from '../../utils/crypto-service';
import { MatDialog } from '@angular/material/dialog';
import { SimpleMessageDialogComponent } from '../../utils/simple-message-dialog/simple-message-dialog.component';

export interface ChallengeDTO {
  secret?: string;
}
@Component({
  selector: 'app-verify',
  imports: [CommonModule],
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.css',
})
export class VerifyComponent implements OnInit {
  token: string = '';
  challengeDTO: any = { secret: null };

  selectedFile: File | null = null;
  selectedFileName: string = '';

  isSuccess: boolean = false;
  verificationStatus: string = 'Verification Pending';
  verificationMessage: string = 'Please upload your private key to continue.';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private cryptoService: CryptoService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token')!;

    this.authService.verify(this.token).subscribe({
      next: (res) => {
        this.challengeDTO = res.body;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.verificationStatus = 'Invalid Link';
        this.verificationMessage = 'This verification link is invalid or has expired.';
        this.isSuccess = false;
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

      const dekriptovano = await this.cryptoService.decryptMessage(
        this.challengeDTO.secret,
        privateKey,
      );

      this.authService.completeVerification(this.token, dekriptovano).subscribe({
        next: () => {
          this.isSuccess = true;
          this.verificationStatus = 'Account Verified!';
          this.verificationMessage = 'Success! You can now log in.';
          this.cdr.detectChanges();
        },
        error: () => {
          this.isSuccess = false;
          this.verificationStatus = 'Verification Failed';
          this.verificationMessage = 'The uploaded key does not match this account.';
          this.cdr.detectChanges();
        },
      });
    } catch (err) {
      this.isSuccess = false;
      this.verificationStatus = 'Error';
      this.verificationMessage = 'Invalid private key file.';
      this.cdr.detectChanges();
    }
  }
}
