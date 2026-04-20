import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-registration',
  imports: [FormsModule],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css',
})
export class RegistrationComponent {
  formData = {
    name: '',
    lastname: '',
    email: '',
    password: '',
    confirmPassword: '',
    organization: '',
  };

  onSubmit(): void {
    console.log('Form submitted:', this.formData);
    // Dodaj logiku za slanje podataka ovdje
  }
}
