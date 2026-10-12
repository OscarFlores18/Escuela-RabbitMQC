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
    this.cargarAlumnos();
    this.cargarCursos();
  }

  // Relacion nota -> alumno (nombre y apellido).
  cargarAlumnos(): void {
    this.alumnoService.obtenerAlumnos().subscribe({
      next: (alumnos) => {
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
        this.mapaCursos = new Map(
          cursos.map((c) => [
            c.id,
            `${c.grado} ${c.division} - ${c.materia}`
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
    return this.mapaAlumnos.get(nota.alumnoId) ?? `#${nota.alumnoId}`;
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

  abrirFormularioNota(): void {

    Swal.fire({
      title: 'Nueva nota',

      html: `
        <input
          id="alumnoId"
          class="swal2-input"
          placeholder="ID del alumno"
          type="number">

        <input
          id="cursoId"
          class="swal2-input"
          placeholder="ID del curso"
          type="number">

        <input
          id="materia"
          class="swal2-input"
          placeholder="Materia">

        <input
          id="nota"
          class="swal2-input"
          placeholder="Nota (0 a 10)"
          type="number"
          step="0.01"
          min="0"
          max="10">

        <input
          id="fecha"
          class="swal2-input"
          placeholder="Fecha"
          type="date">
      `,

      confirmButtonText: 'Guardar nota',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const alumnoId =
          (document.getElementById('alumnoId') as HTMLInputElement).value;

        const cursoId =
          (document.getElementById('cursoId') as HTMLInputElement).value;

        const materia =
          (document.getElementById('materia') as HTMLInputElement).value;

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

      html: `
        <input id="alumnoId" class="swal2-input" type="number"
          placeholder="ID del alumno" value="${nota.alumnoId}">

        <input id="cursoId" class="swal2-input" type="number"
          placeholder="ID del curso" value="${nota.cursoId}">

        <input id="materia" class="swal2-input"
          placeholder="Materia" value="${nota.materia}">

        <input id="nota" class="swal2-input" type="number" step="0.01"
          min="0" max="10" placeholder="Nota (0 a 10)" value="${nota.nota}">

        <input id="fecha" class="swal2-input" type="date"
          placeholder="Fecha" value="${nota.fecha ?? ''}">
      `,

      showCancelButton: true,
      confirmButtonText: 'Guardar cambios',
      cancelButtonText: 'Cancelar',
      focusConfirm: false,

      preConfirm: () => {

        const alumnoId =
          (document.getElementById('alumnoId') as HTMLInputElement).value;

        const cursoId =
          (document.getElementById('cursoId') as HTMLInputElement).value;

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
              Swal.fire('¡Modificada!', 'La nota se actualizó correctamente.', 'success');
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
      text: `Se eliminará la nota de ${nota.materia}.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626'
    }).then((resultado) => {

      if (resultado.isConfirmed) {

        this.notaService.eliminarNota(nota.id).subscribe({

          next: () => {
            Swal.fire('Eliminada', 'La nota fue eliminada.', 'success');
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
