import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { NotaService } from '../../services/nota.service';
import { Nota } from '../../models/nota';
import { AlumnoService } from '../../services/alumno.service';
import { Alumno } from '../../models/alumno';
import { CursoService } from '../../services/curso.service';
import { Curso } from '../../models/curso';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-notas',
  standalone: true,
  templateUrl: './notas.html',
  styleUrl: './notas.css'
})
export class Notas implements OnInit {

  notas: Nota[] = [];
  alumnos: Alumno[] = [];
  cursos: Curso[] = [];
  private mapaAlumnos = new Map<number, string>();
  private mapaCursos = new Map<number, string>();

  constructor(
    private notaService: NotaService,
    private alumnoService: AlumnoService,
    private cursoService: CursoService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarNotas();

    // El nombre del alumno se resuelve desde el modulo Alumnos. El DIRECTOR
    // puede ver notas pero no alumnos, por lo que no intentamos cargarlos.
    if (
      this.authService.tieneRol(
        'ROOT', 'ADMIN', 'DOCENTE', 'PRECEPTOR', 'SECRETARIA', 'ADMINISTRATIVO'
      )
    ) {
      this.cargarAlumnos();
    }

    this.cargarCursos();
  }

  // Relacion nota -> alumno (nombre y apellido).
  cargarAlumnos(): void {
    this.alumnoService.obtenerAlumnos().subscribe({
      next: (alumnos) => {
        this.alumnos = alumnos;
        this.mapaAlumnos = new Map(
          alumnos.map((a) => [a.id, `${a.nombre} ${a.apellido}`])
        );
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener alumnos:', error);
      }
    });
  }

  // Relacion nota -> curso (etiqueta legible).
  cargarCursos(): void {
    this.cursoService.obtenerCursos().subscribe({
      next: (cursos) => {
        this.cursos = cursos;
        this.mapaCursos = new Map(
          cursos.map((c) => [
            c.id,
            `${c.grado} ${c.division}`
          ])
        );
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener cursos:', error);
      }
    });
  }

  alumnoDe(nota: Nota): string {
    return this.mapaAlumnos.get(nota.alumnoId) ?? `Alumno #${nota.alumnoId}`;
  }

  cursoDe(nota: Nota): string {
    return this.mapaCursos.get(nota.cursoId) ?? `#${nota.cursoId}`;
  }

  cargarNotas(): void {
    this.notaService.obtenerNotas().subscribe({
      next: (data) => {
        this.notas = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener notas:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar las notas.'
        });
      }
    });
  }

  private opcionesAlumnos(seleccionado?: number): string {
    const opciones = this.alumnos
      .map(
        (a) =>
          `<option value="${a.id}" ${a.id === seleccionado ? 'selected' : ''}>` +
          `${a.nombre} ${a.apellido}</option>`
      )
      .join('');

    return `<option value="">Seleccione un alumno</option>${opciones}`;
  }

  private opcionesCursos(seleccionado?: number): string {
    const opciones = this.cursos
      .map(
        (c) =>
          `<option value="${c.id}" ${c.id === seleccionado ? 'selected' : ''}>` +
          `${c.grado} ${c.division}</option>`
      )
      .join('');

    return `<option value="">Seleccione un curso</option>${opciones}`;
  }

  abrirFormularioNota(): void {

    Swal.fire({
      title: 'Nueva nota',
      width: '420px',

      html: `
        <select id="alumnoId" class="swal2-select" style="width:90%;">
          ${this.opcionesAlumnos()}
        </select>

        <select id="cursoId" class="swal2-select" style="width:90%;">
          ${this.opcionesCursos()}
        </select>

        <input
          id="materia"
          class="swal2-input"
          placeholder="Materia"
          style="width:90%;">

        <input
          id="nota"
          class="swal2-input"
          placeholder="Nota (0 a 10)"
          type="number"
          step="0.01"
          min="0"
          max="10"
          style="width:90%;">

        <input
          id="fecha"
          class="swal2-input"
          placeholder="Fecha"
          type="date"
          style="width:90%;">
      `,

      confirmButtonText: 'Guardar nota',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const alumnoId =
          (document.getElementById('alumnoId') as HTMLSelectElement).value;

        const cursoId =
          (document.getElementById('cursoId') as HTMLSelectElement).value;

        const materia =
          (document.getElementById('materia') as HTMLInputElement).value.trim();

        const nota =
          (document.getElementById('nota') as HTMLInputElement).value;

        const fecha =
          (document.getElementById('fecha') as HTMLInputElement).value;

        if (!alumnoId || !cursoId || !materia || !nota) {
          Swal.showValidationMessage('Completá todos los campos obligatorios');
          return false;
        }

        const valor = Number(nota);

        if (isNaN(valor) || valor < 0 || valor > 10) {
          Swal.showValidationMessage('La nota debe ser un número entre 0 y 10');
          return false;
        }

        return {
          alumnoId: Number(alumnoId),
          cursoId: Number(cursoId),
          materia,
          nota: valor,
          fecha: fecha || null
        };
      }

    }).then((resultado) => {

      if (resultado.isConfirmed && resultado.value) {

        this.crearNota(resultado.value as Nota);
      }
    });
  }

  crearNota(nota: Nota): void {

    this.notaService.crearNota(nota).subscribe({

      next: () => {

        Swal.fire({
          icon: 'success',
          title: '¡Nota creada!',
          text: 'La nota fue registrada correctamente.',
          confirmButtonText: 'Aceptar'
        });

        this.cargarNotas();
      },

      error: (error) => {

        console.error('Error al crear nota:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo registrar la nota.'
        });
      }
    });
  }

  abrirFormularioEditar(nota: Nota): void {

    Swal.fire({
      title: 'Editar nota',
      width: '420px',

      html: `
        <select id="alumnoId" class="swal2-select" style="width:90%;">
          ${this.opcionesAlumnos(nota.alumnoId)}
        </select>

        <select id="cursoId" class="swal2-select" style="width:90%;">
          ${this.opcionesCursos(nota.cursoId)}
        </select>

        <input id="materia" class="swal2-input"
          placeholder="Materia" value="${nota.materia ?? ''}"
          style="width:90%;">

        <input id="nota" class="swal2-input" type="number" step="0.01"
          min="0" max="10" placeholder="Nota (0 a 10)" value="${nota.nota}"
          style="width:90%;">

        <input id="fecha" class="swal2-input" type="date"
          placeholder="Fecha" value="${nota.fecha ?? ''}"
          style="width:90%;">
      `,

      showCancelButton: true,
      confirmButtonText: 'Guardar cambios',
      cancelButtonText: 'Cancelar',
      focusConfirm: false,

      preConfirm: () => {

        const alumnoId =
          (document.getElementById('alumnoId') as HTMLSelectElement).value;

        const cursoId =
          (document.getElementById('cursoId') as HTMLSelectElement).value;

        const materia =
          (document.getElementById('materia') as HTMLInputElement).value.trim();

        const notaValor =
          (document.getElementById('nota') as HTMLInputElement).value;

        const fecha =
          (document.getElementById('fecha') as HTMLInputElement).value;

        if (!alumnoId || !cursoId || !materia || !notaValor) {
          Swal.showValidationMessage('Completá todos los campos obligatorios');
          return false;
        }

        const valor = Number(notaValor);

        if (isNaN(valor) || valor < 0 || valor > 10) {
          Swal.showValidationMessage('La nota debe ser un número entre 0 y 10');
          return false;
        }

        return {
          alumnoId: Number(alumnoId),
          cursoId: Number(cursoId),
          materia,
          nota: valor,
          fecha: fecha || null
        };
      }

    }).then((resultado) => {

      if (resultado.isConfirmed && resultado.value) {

        this.notaService
          .modificarNota(nota.id, resultado.value as Nota)
          .subscribe({

            next: () => {
              Swal.fire({
                icon: 'success',
                title: '¡Modificada!',
                text: 'La nota se actualizó correctamente.',
                timer: 1800,
                showConfirmButton: false
              });
              this.cargarNotas();
            },

            error: (error) => {
              console.error('Error al modificar nota:', error);
              Swal.fire('Error', 'No se pudo modificar la nota.', 'error');
            }
          });
      }
    });
  }

  confirmarEliminar(nota: Nota): void {

    Swal.fire({
      title: '¿Eliminar nota?',
      text: `Se eliminará la nota de ${this.alumnoDe(nota)} - ${this.cursoDe(nota)}`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626'
    }).then((resultado) => {

      if (resultado.isConfirmed) {

        this.notaService.eliminarNota(nota.id).subscribe({

          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Eliminada',
              text: 'La nota fue eliminada correctamente.',
              timer: 1500,
              showConfirmButton: false
            });
            this.cargarNotas();
          },

          error: (error) => {
            console.error('Error al eliminar nota:', error);
            Swal.fire('Error', 'No se pudo eliminar la nota.', 'error');
          }
        });
      }
    });
  }

}
