import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private alertService = inject(AlertService);
  // Inyectar servicios necesarios
  private authService = inject(AuthService);

  username = '';
  password = '';
  isLoading = false;
  loginError = false;

  // Método para manejar el inicio de sesión
  login():void {
    this.authService.login({usuario:this.username, password:this.password})
    .pipe(finalize(() => this.isLoading = false)) // Finalizar la carga después de la respuesta
    .subscribe({
      next: (success) => {
        if (success) {
          // Cerrar el modal de inicio de sesión y mostrar mensaje de bienvenida
          document.getElementById('CloseLoginModal')?.click(); // Resetear el formulario después del inicio de sesión exitoso
          this.alertService.toast('¡Bienvenido de nuevo!', 'success'); // Mostrar mensaje de éxito
          this.password = ''; // Limpiar el campo de contraseña
        } else {
          this.loginError = true; // Mostrar mensaje de error si las credenciales son incorrectas
          this.alertService.error('Usuario o contraseña incorrectos.', 'Error de inicio de sesión'); // Mostrar alerta de error
        }
  }
})
  }

}
