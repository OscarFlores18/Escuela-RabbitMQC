import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  // No adjuntar el token al login: si hay un token viejo/expirado en
  // localStorage, el gateway responde 401 SIN headers CORS y el navegador
  // reporta un falso error de CORS que impide iniciar sesion.
  const esLogin = req.url.includes('/auth/login');

  if (isPlatformBrowser(platformId) && !esLogin) {
    const token = localStorage.getItem('token');

    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  }

  return next(req);
};
