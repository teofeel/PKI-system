import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CertificateSigningRequest } from './certificate-signing-request';

describe('CertificateSigningRequest', () => {
  let component: CertificateSigningRequest;
  let fixture: ComponentFixture<CertificateSigningRequest>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CertificateSigningRequest]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CertificateSigningRequest);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
