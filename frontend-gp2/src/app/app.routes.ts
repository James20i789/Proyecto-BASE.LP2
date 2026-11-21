import { Routes } from '@angular/router';
import { accountGuard } from './core/account.guard';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';

export const routes: Routes = [{
  path: '',
  component: MainLayoutComponent,
  children: [
    { path: '', pathMatch: 'full', redirectTo: 'home' },
    { path: 'home', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
    { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
    { path: 'registro', loadComponent: () => import('./features/register/register.component').then(m => m.RegisterComponent) },
    { path: 'carrito', loadComponent: () => import('./features/carrito/carrito.component').then(m => m.CarritoComponent) },
    { path: 'pedidos', canActivate: [accountGuard('CLIENTE')],
      loadComponent: () => import('./features/pedidos/pedidos.component').then(m => m.PedidosComponent) },
    { path: 'pedido/:id', canActivate: [accountGuard('CLIENTE')],
      loadComponent: () => import('./features/pedidos/pedido.component').then(m => m.PedidoComponent) },
    { path: 'productos', canActivate: [accountGuard('ADMIN')],
      loadComponent: () => import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent) },
    { path: 'productos/nuevo', canActivate: [accountGuard('ADMIN')],
      loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
    { path: 'productos/editar/:id', canActivate: [accountGuard('ADMIN')],
      loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
    { path: '**', redirectTo: 'home' }
  ]
}];
