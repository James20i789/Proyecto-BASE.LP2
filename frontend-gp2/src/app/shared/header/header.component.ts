import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { RegisterComponent } from '../../features/register/register.component';
import { LoginComponent } from '../../features/login/login.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule,RouterLink, LoginComponent, RegisterComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  public cartService = inject(CartService);
  public authService = inject(AuthService);
  userName: String = '';
  isAdmin: boolean = false;

  // Implementación del método ngOnInit para suscribirse al estado de inicio de sesión
  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        // Recuperar el nombre de usuario y el rol del usuario desde localStorage
        this.userName = localStorage.getItem('user_name') || 'Usuario';
        this.isAdmin = localStorage.getItem('user_role') === 'ADMIN';
      } else {
        this.userName = '';
        this.isAdmin = false;
      }
  })
  }
  // Método para cerrar sesión
  logout(): void {
    this.authService.logout();
  }

}
