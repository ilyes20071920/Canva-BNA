import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm: FormGroup = this.fb.group({
    matricule: ['', [Validators.required]],
    password: ['', Validators.required]
  });

  errorMessage: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      const credentials = {
        matricule: Number(this.loginForm.value.matricule),
        password: this.loginForm.value.password
      };

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.isLoading = false;
          // Redirect to a dashboard or home page after successful login
          // Assuming /dashboard exists, or you can route wherever needed.
          this.router.navigate(['/dashboard']); 
        },
        error: (error) => {
          this.isLoading = false;
          if (error.error && error.error.message === 'Account is disabled') {
            this.errorMessage = 'Ce compte utilisateur est désactivé.';
          } else {
            this.errorMessage = 'Identifiant ou mot de passe incorrect.';
          }
          console.error('Login error', error);
        }
      });
    }
  }
}
