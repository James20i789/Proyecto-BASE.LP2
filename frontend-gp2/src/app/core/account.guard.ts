import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { Account } from './Interfaces/auth.interface';
import { AuthService } from './services/auth.service';

export function accountGuard(role: Account['rol']): CanActivateFn {
  return (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.session().pipe(map(user => {
      if (!user) {
        return router.createUrlTree(['/login'], { queryParams: { volver: state.url.split('?')[0] } });
      }
      return user.rol === role ? true : router.createUrlTree(['/home']);
    }));
  };
}

export function accountReturnPath(value: string | null): string {
  return value && /^\/(?:home|carrito|pedidos|pedido\/[0-9]+|productos(?:\/nuevo|\/editar\/[0-9]+)?)$/.test(value)
    ? value : '/home';
}
