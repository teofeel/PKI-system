import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verify',
  imports: [CommonModule],
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.css',
})
export class VerifyComponent implements OnInit {
  verificationStatus: string = '';
  verificationMessage: string = '';
  isSuccess: boolean = true;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.authService.verify(this.route.snapshot.paramMap.get('token')!).subscribe({
      next: () => {
        this.verificationStatus = 'Account Succesfully Verified!';
        this.verificationMessage = 'You can log in with your account now';
        this.isSuccess = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.verificationStatus = 'Invalid Verification link!';
        this.verificationMessage = 'Verification link has expired or has been already used!';
        this.isSuccess = false;
        this.cdr.detectChanges();
      },
    });
  }
}
