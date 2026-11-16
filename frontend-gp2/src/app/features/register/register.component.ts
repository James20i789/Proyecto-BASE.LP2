import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { AlertService } from '../../core/services/alert.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private alertService = inject(AlertService);

  // DATOS DEL ARREGLO
  userData = {
    nombre: '',
    email: '',
    telefono: '',
    direccion: '',
    usuario: '',
    password: ''
  };

  isloading = false;
  // MÉTODO PARA REGISTRAR UN NUEVO USUARIO
  registrar():void {
    if(this.isloading) return; // Evitar múltiples solicitudes si ya se está cargando
    this.isloading = true; // Indicar que se está cargando

    this.authService.registrar(this.userData)
    .pipe(finalize(() => this.isloading = false)) // Finalizar la carga después de la respuesta
    .subscribe({
      // Manejar la respuesta del registro
      next: () => {
        // (1) CERRAR EL MODAL DE REGISTRO
        document.getElementById('CloseRegisterModal')?.click(); // Cerrar el modal de registro
        // (2) MOSTRAR MENSAJE DE ÉXITO - ALERTA
        this.alertService.success('¡Registro exitoso! Ahora puedes iniciar sesión.', 'success'); // Mostrar mensaje de éxito
      }
    })
}
}
