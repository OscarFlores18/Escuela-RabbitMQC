import { Routes } from '@angular/router';
import { Alumnos } from './components/alumnos/alumnos';
import { Cursos } from './components/cursos/cursos';
import { Notas } from './components/notas/notas';
import { Administracion } from './components/administracion/administracion';
import { Usuarios } from './components/usuarios/usuarios';
import { Login } from './components/login/login';
import { MisCursos } from './components/mis-cursos/mis-cursos';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';

export const routes: Routes = [

  {
    path: 'login',
    component: Login
  },

  // Alumnos: gestion de alumnos (el director NO accede)
  {
    path: 'alumnos',
    component: Alumnos,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'SECRETARIA', 'PRECEPTOR', 'DOCENTE'])
    ]
  },

  // Cursos: el director accede en modo solo lectura
  {
    path: 'cursos',
    component: Cursos,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'SECRETARIA', 'PRECEPTOR', 'DOCENTE'])
    ]
  },

  // Notas: el director accede en modo solo lectura; el preceptor NO accede
  {
    path: 'notas',
    component: Notas,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'DIRECTOR', 'SECRETARIA', 'DOCENTE'])
    ]
  },

  {
    path: 'mis-cursos',
    component: MisCursos,
    canActivate: [
      authGuard,
      roleGuard(['DOCENTE'])
    ]
  },

  // Administración: gestion de personal (el director NO accede)
  {
    path: 'administracion',
    component: Administracion,
    canActivate: [
      authGuard,
      roleGuard(['ROOT', 'ADMIN', 'ADMINISTRATIVO'])
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
