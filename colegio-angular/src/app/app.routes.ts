import { Routes } from '@angular/router';
import { Alumnos } from './components/alumnos/alumnos';
import { Cursos } from './components/cursos/cursos';
import { Notas } from './components/notas/notas';
import { Administracion } from './components/administracion/administracion';
import { Usuarios } from './components/usuarios/usuarios';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';

export const routes: Routes = [

  {
    path: 'login',
    component: Login
  },

  {
    path: 'alumnos',
    component: Alumnos,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'SECRETARIA', 'PRECEPTOR', 'DOCENTE'])
    ]
  },

  {
    path: 'cursos',
    component: Cursos,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'SECRETARIA', 'PRECEPTOR', 'DOCENTE'])
    ]
  },

  {
    path: 'notas',
    component: Notas,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'SECRETARIA', 'PRECEPTOR', 'DOCENTE'])
    ]
  },

  {
    path: 'administracion',
    component: Administracion,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'ADMINISTRATIVO'])
    ]
  },

  {
    path: 'usuarios',
    component: Usuarios,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN'])
    ]
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }

];
