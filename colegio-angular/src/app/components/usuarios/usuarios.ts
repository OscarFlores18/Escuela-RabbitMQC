import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { UsuarioService, UsuarioPayload } from '../../services/usuario.service';
import { Usuario } from '../../models/usuario';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {

  usuarios: Usuario[] = [];

  roles: string[] = [
    'ROOT',
    'ADMIN',
    'DIRECTOR',
    'SECRETARIA',
    'PRECEPTOR',
    'DOCENTE',
    'ADMINISTRATIVO'
  ];

  constructor(
    private usuarioService: UsuarioService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.usuarioService.obtenerUsuarios().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener usuarios:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar los usuarios.'
        });
      }
    });
  }

  private opcionesRoles(seleccionado?: string): string {
    return this.roles
      .map(rol => `<option value="${rol}" ${rol === seleccionado ? 'selected' : ''}>${rol}</option>`)
      .join('');
  }

  abrirFormularioUsuario(): void {

    Swal.fire({
      title: 'Nuevo usuario',

      html: `
        <input id="username" class="swal2-input" placeholder="Usuario">

        <input id="password" class="swal2-input" type="password" placeholder="Contraseña">

        <select id="rol" class="swal2-select">
          <option value="">Seleccionar rol</option>
          ${this.opcionesRoles()}
        </select>
      `,

      confirmButtonText: 'Guardar usuario',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const username = (document.getElementById('username') as HTMLInputElement).value;
        const password = (document.getElementById('password') as HTMLInputElement).value;
        const rol = (document.getElementById('rol') as HTMLSelectElement).value;

        if (!username || !password || !rol) {
          Swal.showValidationMessage('Completá todos los campos');
          return false;
        }

        return { username, password, rol };
      }

    }).then((resultado) => {
      if (resultado.isConfirmed && resultado.value) {
        this.crearUsuario(resultado.value as UsuarioPayload);
      }
    });
  }

  crearUsuario(usuario: UsuarioPayload): void {

    this.usuarioService.crearUsuario(usuario).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: '¡Usuario creado!',
          text: 'El usuario fue registrado correctamente.',
          confirmButtonText: 'Aceptar'
        });
        this.cargarUsuarios();
      },
      error: (error) => {
        console.error('Error al crear usuario:', error);

        const mensaje = error.status === 409
          ? 'Ya existe un usuario con ese nombre.'
          : error.status === 403
            ? 'No tenés permisos para esta operación.'
            : 'No se pudo registrar el usuario.';

        Swal.fire({ icon: 'error', title: 'Error', text: mensaje });
      }
    });
  }

  abrirFormularioEditar(usuario: Usuario): void {

    Swal.fire({
      title: 'Editar usuario',

      html: `
        <input id="username" class="swal2-input" placeholder="Usuario" value="${usuario.username}">

        <input id="password" class="swal2-input" type="password" placeholder="Nueva contraseña (opcional)">

        <select id="rol" class="swal2-select">
          ${this.opcionesRoles(usuario.rol)}
        </select>
      `,

      showCancelButton: true,
      confirmButtonText: 'Guardar cambios',
      cancelButtonText: 'Cancelar',
      focusConfirm: false,

      preConfirm: () => {

        const username = (document.getElementById('username') as HTMLInputElement).value.trim();
        const password = (document.getElementById('password') as HTMLInputElement).value;
        const rol = (document.getElementById('rol') as HTMLSelectElement).value;

        if (!username || !rol) {
          Swal.showValidationMessage('Completá usuario y rol');
          return false;
        }

        return { username, password, rol };
      }

    }).then((resultado) => {
      if (resultado.isConfirmed && resultado.value) {
        this.usuarioService
          .modificarUsuario(usuario.id, resultado.value as UsuarioPayload)
          .subscribe({
            next: () => {
              Swal.fire('¡Modificado!', 'El usuario se actualizó correctamente.', 'success');
              this.cargarUsuarios();
            },
            error: (error) => {
              console.error('Error al modificar usuario:', error);

              const mensaje = error.status === 409
                ? 'Ya existe un usuario con ese nombre.'
                : 'No se pudo modificar el usuario.';

              Swal.fire('Error', mensaje, 'error');
            }
          });
      }
    });
  }

  confirmarEliminar(usuario: Usuario): void {

    Swal.fire({
      title: '¿Eliminar usuario?',
      text: `Se eliminará el usuario ${usuario.username}.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626'
    }).then((resultado) => {
      if (resultado.isConfirmed) {
        this.usuarioService.eliminarUsuario(usuario.id).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'El usuario fue eliminado.', 'success');
            this.cargarUsuarios();
          },
          error: (error) => {
            console.error('Error al eliminar usuario:', error);
            Swal.fire('Error', 'No se pudo eliminar el usuario.', 'error');
          }
        });
      }
    });
  }

}
