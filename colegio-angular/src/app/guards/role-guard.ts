import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const roleGuard = (rolesPermitidos: string[]): CanActivateFn => {

  return () => {

    const authService = inject(AuthService);
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);

    if (!isPlatformBrowser(platformId)) {
      return true;
    }

    const rol = authService.obtenerRol();

    if (rol && rolesPermitidos.includes(rol)) {
      return true;
    }

    // Redirige a la pantalla inicial segun el rol. No se manda siempre a
    // /alumnos porque hay roles (ej. DIRECTOR) que no tienen acceso a esa vista.
    if (rol === 'DOCENTE') {
      return router.createUrlTree(['/mis-cursos']);
    }

    if (rol === 'ADMINISTRATIVO') {
      return router.createUrlTree(['/administracion']);
    }

    if (rol) {
      return router.createUrlTree(['/cursos']);
    }

    return router.createUrlTree(['/login']);
  };
};
