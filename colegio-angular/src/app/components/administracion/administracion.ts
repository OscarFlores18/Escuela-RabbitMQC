import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { AdministracionService } from '../../services/administracion.service';
import { Personal } from '../../models/personal';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-administracion',
  standalone: true,
  templateUrl: './administracion.html',
  styleUrl: './administracion.css'
})
export class Administracion implements OnInit {

  personal: Personal[] = [];

constructor(
  private administracionService: AdministracionService,
  public authService: AuthService,
  private cdr: ChangeDetectorRef
) {}

  ngOnInit(): void {
    this.cargarPersonal();
  }

  cargarPersonal(): void {
    this.administracionService.obtenerPersonal().subscribe({
      next: (data) => {
        this.personal = data;
          this.cdr.detectChanges();

      },

      error: (error) => {
        console.error('Error al obtener personal:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo cargar el personal.'
        });
      }
    });
  }

  abrirFormularioPersonal(): void {

    Swal.fire({
      title: 'Nuevo personal',

      html: `
        <input
          id="nombre"
          class="swal2-input"
          placeholder="Nombre">

        <input
          id="apellido"
          class="swal2-input"
          placeholder="Apellido">

        <input
          id="email"
          class="swal2-input"
          placeholder="Email"
          type="email">

        <select
          id="cargo"
          class="swal2-select">

          <option value="">Seleccionar cargo</option>
          <option value="DIRECTOR">Director</option>
          <option value="VICEDIRECTOR">Vicedirector</option>
          <option value="SECRETARIO">Secretario</option>
          <option value="PRECEPTOR">Preceptor</option>
          <option value="DOCENTE">Docente</option>
          <option value="ADMINISTRATIVO">Administrativo</option>

        </select>
      `,

      confirmButtonText: 'Guardar personal',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const nombre =
          (document.getElementById('nombre') as HTMLInputElement).value;

        const apellido =
          (document.getElementById('apellido') as HTMLInputElement).value;

        const email =
          (document.getElementById('email') as HTMLInputElement).value;

        const cargo =
          (document.getElementById('cargo') as HTMLSelectElement).value;

        if (!nombre || !apellido || !email || !cargo) {

          Swal.showValidationMessage(
            'Completá todos los campos'
          );

          return false;
        }

        if (!email.includes('@')) {

          Swal.showValidationMessage(
            'Ingresá un email válido'
          );

          return false;
        }

        return {
          nombre,
          apellido,
          email,
          cargo
        };
      }

    }).then((resultado) => {

      if (resultado.isConfirmed) {

        const personal: Personal = resultado.value;

        this.crearPersonal(personal);
      }
    });
  }

  crearPersonal(personal: Personal): void {

    this.administracionService.crearPersonal(personal).subscribe({

      next: () => {

        Swal.fire({
          icon: 'success',
          title: '¡Personal creado!',
          text: 'El personal fue registrado correctamente.',
          confirmButtonText: 'Aceptar'
        });

        this.cargarPersonal();
      },

      error: (error) => {

        console.error('Error al crear personal:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo registrar el personal.'
        });
      }
    });
  }

  abrirFormularioEditar(persona: Personal): void {
    const cargos = [
      'DIRECTOR',
      'VICEDIRECTOR',
      'SECRETARIO',
      'PRECEPTOR',
      'DOCENTE',
      'ADMINISTRATIVO'
    ];

    Swal.fire({
      title: 'Editar personal',
      html: `
        <input id="nombre" class="swal2-input" placeholder="Nombre">
        <input id="apellido" class="swal2-input" placeholder="Apellido">
        <input id="email" class="swal2-input" type="email" placeholder="Email">
        <select id="cargo" class="swal2-select">
          <option value="">Seleccionar cargo</option>
          ${cargos.map(cargo => `<option value="${cargo}">${cargo}</option>`).join('')}
        </select>
      `,
      showCancelButton: true,
      confirmButtonText: 'Guardar cambios',
      cancelButtonText: 'Cancelar',
      focusConfirm: false,

      didOpen: () => {
        (document.getElementById('nombre') as HTMLInputElement).value = persona.nombre;
        (document.getElementById('apellido') as HTMLInputElement).value = persona.apellido;
        (document.getElementById('email') as HTMLInputElement).value = persona.email;
        (document.getElementById('cargo') as HTMLSelectElement).value = persona.cargo;
      },

      preConfirm: () => {
        const nombre = (document.getElementById('nombre') as HTMLInputElement).value.trim();
        const apellido = (document.getElementById('apellido') as HTMLInputElement).value.trim();
        const email = (document.getElementById('email') as HTMLInputElement).value.trim();
        const cargo = (document.getElementById('cargo') as HTMLSelectElement).value;

        if (!nombre || !apellido || !email || !cargo) {
          Swal.showValidationMessage('Completá todos los campos');
          return false;
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
          Swal.showValidationMessage('Ingresá un email válido');
          return false;
        }

        return { nombre, apellido, email, cargo };
      }
    }).then((resultado) => {
      if (resultado.isConfirmed && resultado.value) {
        this.administracionService
          .modificarPersonal(persona.id, resultado.value)
          .subscribe({
            next: () => {
              Swal.fire('¡Modificado!', 'Los datos se actualizaron correctamente.', 'success');
              this.cargarPersonal();
            },
            error: (error) => {
              console.error('Error al modificar personal:', error);
              Swal.fire('Error', 'No se pudo modificar el personal.', 'error');
            }
          });
      }
    });
  }

  confirmarEliminar(persona: Personal): void {
    Swal.fire({
      title: '¿Eliminar personal?',
      text: `Se eliminará a ${persona.nombre} ${persona.apellido}.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626'
    }).then((resultado) => {
      if (resultado.isConfirmed) {
        this.administracionService.eliminarPersonal(persona.id).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'El registro fue eliminado correctamente.', 'success');
            this.cargarPersonal();
          },
          error: (error) => {
            console.error('Error al eliminar personal:', error);
            Swal.fire('Error', 'No se pudo eliminar el registro.', 'error');
          }
        });
      }
    });
  }

}