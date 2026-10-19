import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { UsuarioService, UsuarioPayload } from '../../services/usuario.service';
import { Usuario } from '../../models/usuario';
import { AdministracionService } from '../../services/administracion.service';
import { Personal } from '../../models/personal';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

type Vista = 'registrados' | 'sin-cuenta';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {

  usuarios: Usuario[] = [];
  personal: Personal[] = [];

  vista: Vista = 'registrados';

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
    private administracionService: AdministracionService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarTodo();
  }

  // ROOT es el unico que puede administrar cuentas.
  get esRoot(): boolean {
    return this.authService.tieneRol('ROOT');
  }

  cargarTodo(): void {
    this.cargarUsuarios();
    this.cargarPersonal();
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

  cargarPersonal(): void {
    this.administracionService.obtenerPersonal().subscribe({
      next: (data) => {
        this.personal = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener personal:', error);
      }
    });
  }

  cambiarVista(vista: Vista): void {
    this.vista = vista;
  }

  // Personas registradas en personal que todavia no tienen cuenta de acceso.
  get personalSinCuenta(): Personal[] {
    return this.personal.filter(
      (p) => !this.usuarios.some((u) => u.personalId === p.id)
    );
  }

  // Persona del colegio vinculada a una cuenta (si corresponde).
  personaDe(usuario: Usuario): string {
    if (usuario.personalId == null) {
      return '—';
    }
    const persona = this.personal.find((p) => p.id === usuario.personalId);
    return persona ? `${persona.nombre} ${persona.apellido}` : `#${usuario.personalId}`;
  }

  // Clase de badge segun el rol, para destacarlo visualmente.
  claseRol(rol: string): string {
    switch (rol) {
      case 'ROOT':
        return 'badge badge--red';
      case 'ADMIN':
        return 'badge badge--orange';
      case 'DIRECTOR':
        return 'badge badge--green';
      default:
        return 'badge badge--blue';
    }
  }

  private opcionesRoles(seleccionado?: string): string {
    return this.roles
      .map(rol => `<option value="${rol}" ${rol === seleccionado ? 'selected' : ''}>${rol}</option>`)
      .join('');
  }

  private opcionesPersonal(seleccionado?: number): string {
    const opciones = this.personalSinCuenta
      .map(
        (p) =>
          `<option value="${p.id}" ${p.id === seleccionado ? 'selected' : ''}>` +
          `${p.nombre} ${p.apellido} (${p.cargo})</option>`
      )
      .join('');

    return `<option value="">Seleccione una persona</option>${opciones}`;
  }

  // Abre el formulario de creacion de cuenta.
  // Si viene una persona (desde "Personal sin cuenta"), queda preseleccionada.
  abrirFormularioUsuario(persona?: Personal): void {

    if (this.personalSinCuenta.length === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Sin personal pendiente',
        text: 'No hay personas registradas sin cuenta de acceso.'
      });
      return;
    }

    Swal.fire({
      title: 'Crear cuenta',

      html: `
        <select id="personalId" class="swal2-select" style="width:90%;">
          ${this.opcionesPersonal(persona?.id)}
        </select>

        <input id="username" class="swal2-input" placeholder="Usuario" style="width:90%;">

        <input id="password" class="swal2-input" type="password" placeholder="Contraseña" style="width:90%;">

        <select id="rol" class="swal2-select" style="width:90%;">
          <option value="">Seleccionar rol</option>
          ${this.opcionesRoles()}
        </select>
      `,

      confirmButtonText: 'Guardar usuario',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const personalId = (document.getElementById('personalId') as HTMLSelectElement).value;
        const username = (document.getElementById('username') as HTMLInputElement).value.trim();
        const password = (document.getElementById('password') as HTMLInputElement).value;
        const rol = (document.getElementById('rol') as HTMLSelectElement).value;

        if (!personalId || !username || !password || !rol) {
          Swal.showValidationMessage('Completá todos los campos');
          return false;
        }

        return { username, password, rol, personalId: Number(personalId) };
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
          title: '¡Cuenta creada!',
          text: 'La cuenta quedó vinculada a la persona seleccionada.',
          confirmButtonText: 'Aceptar'
        });
        this.cargarTodo();
      },
      error: (error) => {
        console.error('Error al crear usuario:', error);

        const mensaje = error.status === 409
          ? (error.error?.mensaje ?? 'La persona ya tiene una cuenta o el usuario ya existe.')
          : error.status === 403
            ? 'No tenés permisos para esta operación.'
            : 'No se pudo crear la cuenta.';

        Swal.fire({ icon: 'error', title: 'Error', text: mensaje });
      }
    });
  }

  abrirFormularioEditar(usuario: Usuario): void {

    Swal.fire({
      title: 'Editar usuario',

      html: `
        <input id="username" class="swal2-input" placeholder="Usuario" value="${usuario.username}" style="width:90%;">

        <input id="password" class="swal2-input" type="password" placeholder="Nueva contraseña (opcional)" style="width:90%;">

        <select id="rol" class="swal2-select" style="width:90%;">
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

        return { username, password, rol, personalId: usuario.personalId };
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
            this.cargarTodo();
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
