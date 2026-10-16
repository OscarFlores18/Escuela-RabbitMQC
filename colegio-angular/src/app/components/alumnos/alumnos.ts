import { Component, ChangeDetectorRef, OnInit  } from '@angular/core';import { ReactiveFormsModule } from '@angular/forms';
import { AlumnoService } from '../../services/alumno.service';
import { Alumno } from '../../models/alumno';
import { CursoService } from '../../services/curso.service';
import { Curso } from '../../models/curso';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-alumnos',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './alumnos.html',
  styleUrl: './alumnos.css'
})
export class Alumnos implements OnInit {

  alumnos: Alumno[] = [];
  cursos: Curso[] = [];
  private mapaCursoPorAlumno = new Map<number, string>();

  constructor(
    private alumnoService: AlumnoService,
    private cursoService: CursoService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    console.log('🔥 ALUMNOS COMPONENTE CREADO');
  }

  ngOnInit(): void {
    console.log('🔥 EJECUTANDO CARGAR ALUMNOS');
    this.cargarAlumnos();
    this.cargarCursos();
  }

  // Relacion alumno -> curso: los cursos traen los ids de sus alumnos inscriptos.
  cargarCursos(): void {
    this.cursoService.obtenerCursos().subscribe({
      next: (cursos) => {
        this.cursos = cursos;
        const mapa = new Map<number, string>();
        cursos.forEach((curso) => {
          const etiqueta = `${curso.grado} ${curso.division} - ${curso.materia}`;
          (curso.alumnoIds ?? []).forEach((alumnoId) => {
            mapa.set(alumnoId, etiqueta);
          });
        });
        this.mapaCursoPorAlumno = mapa;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener cursos:', error);
      }
    });
  }

  cursoDe(alumnoId: number): string {
    return this.mapaCursoPorAlumno.get(alumnoId) ?? '—';
  }

  // Id del curso en el que esta inscripto el alumno (si lo esta).
  private cursoIdDeAlumno(alumnoId: number): number | undefined {
    const curso = this.cursos.find(
      (c) => (c.alumnoIds ?? []).includes(alumnoId)
    );
    return curso?.id;
  }

  // Opciones de cursos para los formularios (alta y edicion).
  private opcionesCursos(seleccionado?: number): string {
    const opciones = this.cursos
      .map(
        (c) =>
          `<option value="${c.id}" ${c.id === seleccionado ? 'selected' : ''}>` +
          `${c.grado} ${c.division} - ${c.materia}</option>`
      )
      .join('');

    return `<option value="">Sin curso / Seleccione un curso</option>${opciones}`;
  }

  cargarAlumnos(): void {

    console.log('🔥 CARGANDO ALUMNOS...');

    this.alumnoService.obtenerAlumnos().subscribe({

      next: (data) => {

        console.log('🔥 ALUMNOS RECIBIDOS:', data);

        this.alumnos = data;

        // 🔥 Fuerza a Angular a actualizar la pantalla
        this.cdr.detectChanges();

      },

      error: (error) => {

        console.error('Error al obtener alumnos:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar los alumnos.'
        });

      }

    });

  }


  abrirFormularioAlumno(): void {

    Swal.fire({

      title: 'Nuevo alumno',

      width: '420px',

      html: `
        <input
          id="nombre"
          class="swal2-input"
          placeholder="Nombre"
          style="width:90%;">

        <input
          id="apellido"
          class="swal2-input"
          placeholder="Apellido"
          style="width:90%;">

        <input
          id="dni"
          class="swal2-input"
          placeholder="DNI"
          type="text"
          style="width:90%;">

        <input
          id="email"
          class="swal2-input"
          placeholder="Email"
          type="email"
          style="width:90%;">

        <select
          id="cursoId"
          class="swal2-select"
          style="width:90%;">
          ${this.opcionesCursos()}
        </select>
      `,

      confirmButtonText: 'Guardar alumno',
      cancelButtonText: 'Cancelar',

      showCancelButton: true,

      focusConfirm: false,

      preConfirm: () => {

        const nombre = (
          document.getElementById('nombre') as HTMLInputElement
        ).value;

        const apellido = (
          document.getElementById('apellido') as HTMLInputElement
        ).value;

        const dni = (
          document.getElementById('dni') as HTMLInputElement
        ).value;

        const email = (
          document.getElementById('email') as HTMLInputElement
        ).value;

        const cursoId = (
          document.getElementById('cursoId') as HTMLSelectElement
        ).value;


        // VALIDACIONES

        if (!nombre || !apellido || !dni || !email) {

          Swal.showValidationMessage(
            'Completá todos los campos'
          );

          return false;
        }


        if (!/^[0-9]+$/.test(dni)) {

          Swal.showValidationMessage(
            'El DNI debe contener solamente números'
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
          dni,
          email,
          cursoId: cursoId ? Number(cursoId) : undefined
        };

      }

    }).then((resultado) => {

      if (resultado.isConfirmed) {

        const alumno: Alumno = resultado.value;

        this.crearAlumno(alumno);

      }

    });

  }

abrirFormularioEdicion(alumno: Alumno): void {
  Swal.fire({
    title: 'Editar alumno',
    width: '420px',
    html: `
      <input id="nombre"
        class="swal2-input"
        placeholder="Nombre"
        value="${alumno.nombre}"
        style="width:90%;">

      <input id="apellido"
        class="swal2-input"
        placeholder="Apellido"
        value="${alumno.apellido}"
        style="width:90%;">

      <input id="dni"
        class="swal2-input"
        placeholder="DNI"
        value="${alumno.dni}"
        style="width:90%;">

      <input id="email"
        class="swal2-input"
        type="email"
        placeholder="Email"
        value="${alumno.email}"
        style="width:90%;">

      <select id="cursoId" class="swal2-select" style="width:90%;">
        ${this.opcionesCursos(this.cursoIdDeAlumno(alumno.id))}
      </select>
    `,
    showCancelButton: true,
    confirmButtonText: 'Guardar cambios',
    cancelButtonText: 'Cancelar',
    focusConfirm: false,
    preConfirm: () => {
      const nombre =
        (document.getElementById('nombre') as HTMLInputElement).value;
      const apellido =
        (document.getElementById('apellido') as HTMLInputElement).value;
      const dni =
        (document.getElementById('dni') as HTMLInputElement).value;
      const email =
        (document.getElementById('email') as HTMLInputElement).value;
      const cursoId =
        (document.getElementById('cursoId') as HTMLSelectElement).value;

      if (!nombre || !apellido || !dni || !email) {
        Swal.showValidationMessage(
          'Completá todos los campos'
        );
        return false;
      }

      if (!/^[0-9]+$/.test(dni)) {
        Swal.showValidationMessage(
          'El DNI debe contener solamente números'
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
        dni,
        email,
        cursoId: cursoId ? Number(cursoId) : undefined
      };
    }
  }).then(resultado => {
    if (resultado.isConfirmed) {
      this.modificarAlumno(
        alumno.id,
        resultado.value as Alumno
      );
    }
  });
}

modificarAlumno(id: number, alumno: Alumno): void {
  this.alumnoService.modificarAlumno(id, alumno)
    .subscribe({
      next: () => {
        Swal.fire(
          '¡Modificado!',
          'Los datos del alumno fueron actualizados.',
          'success'
        );
        this.cargarAlumnos();
      },
      error: error => {
        console.error(error);
        Swal.fire(
          'Error',
          'No se pudo modificar el alumno.',
          'error'
        );
      }
    });
}
confirmarEliminacion(alumno: Alumno): void {
  Swal.fire({
    title: '¿Eliminar alumno?',
    text: `Se eliminará a ${alumno.nombre} ${alumno.apellido}.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  }).then(resultado => {
    if (resultado.isConfirmed) {
      this.eliminarAlumno(alumno.id);
    }
  });
}

eliminarAlumno(id: number): void {
  this.alumnoService.eliminarAlumno(id)
    .subscribe({
      next: () => {
        Swal.fire(
          '¡Eliminado!',
          'El alumno fue dado de baja.',
          'success'
        );
        this.cargarAlumnos();
      },
      error: error => {
        console.error(error);
        Swal.fire(
          'Error',
          'No se pudo eliminar el alumno.',
          'error'
        );
      }
    });
}

  crearAlumno(alumno: Alumno): void {

    this.alumnoService.crearAlumno(alumno).subscribe({

      next: () => {

        Swal.fire({
          icon: 'success',
          title: '¡Alumno creado!',
          text: 'El alumno fue registrado correctamente.',
          confirmButtonText: 'Aceptar'
        });

        this.cargarAlumnos();

      },

      error: (error) => {

        console.error('Error al crear alumno:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo registrar el alumno.'
        });

      }

    });

  }

}