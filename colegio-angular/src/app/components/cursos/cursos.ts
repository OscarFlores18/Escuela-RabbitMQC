import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CursoService } from '../../services/curso.service';
import { Curso } from '../../models/curso';
import { Personal } from '../../models/personal';
import { AdministracionService } from '../../services/administracion.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cursos',
  standalone: true,
  templateUrl: './cursos.html',
  styleUrl: './cursos.css'
})
export class Cursos implements OnInit {

  cursos: Curso[] = [];
  docentes: Personal[] = [];
  private mapaDocentes = new Map<number, string>();

  constructor(
    private cursoService: CursoService,
    private administracionService: AdministracionService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCursos();
    this.cargarDocentes();
  }

  cargarDocentes(): void {
    this.administracionService.obtenerPersonalBasico().subscribe({
      next: (data) => {
        this.docentes = data;
        this.mapaDocentes = new Map(
          data.map((d) => [
            d.id,
            `${d.nombre} ${d.apellido}${d.cargo ? ' (' + d.cargo + ')' : ''}`
          ])
        );
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener docentes:', error);
      }
    });
  }

  docenteDe(curso: Curso): string {
    if (curso.docenteId == null) {
      return '—';
    }
    return this.mapaDocentes.get(curso.docenteId) ?? `#${curso.docenteId}`;
  }

  cupoLleno(curso: Curso): boolean {
    const cupo = curso.cupo_maximo ?? 0;
    return cupo > 0 && (curso.cantidadAlumnos ?? 0) >= cupo;
  }

  porcentajeCupo(curso: Curso): number {
    const cupo = curso.cupo_maximo ?? 0;
    if (cupo <= 0) {
      return 0;
    }
    return Math.min(100, Math.round(((curso.cantidadAlumnos ?? 0) / cupo) * 100));
  }

  private opcionesDocentes(seleccionado?: number): string {
    const opciones = this.docentes
      .map(
        (d) =>
          `<option value="${d.id}" ${d.id === seleccionado ? 'selected' : ''}>` +
          `${d.nombre} ${d.apellido}${d.cargo ? ' (' + d.cargo + ')' : ''}</option>`
      )
      .join('');

    return `<option value="">Sin docente</option>${opciones}`;
  }

  cargarCursos(): void {
    this.cursoService.obtenerCursos().subscribe({
      next: (data) => {
        this.cursos = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener cursos:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar los cursos.'
        });
      }
    });
  }

  abrirFormularioCurso(): void {

    Swal.fire({
      title: 'Nuevo curso',

      html: `
        <input
          id="ciclo_lectivo"
          class="swal2-input"
          placeholder="Ciclo lectivo">

        <input
          id="division"
          class="swal2-input"
          placeholder="División">

        <input
          id="grado"
          class="swal2-input"
          placeholder="Grado">

        <input
          id="turno"
          class="swal2-input"
          placeholder="Turno">

        <input
          id="horario"
          class="swal2-input"
          placeholder="Horario">

        <input
          id="cupo_maximo"
          class="swal2-input"
          placeholder="Cupo máximo"
          type="number">

        <select id="docenteId" class="swal2-select">
          ${this.opcionesDocentes()}
        </select>
      `,

      confirmButtonText: 'Guardar curso',
      cancelButtonText: 'Cancelar',
      showCancelButton: true,
      focusConfirm: false,

      preConfirm: () => {

        const ciclo_lectivo =
          (document.getElementById('ciclo_lectivo') as HTMLInputElement).value;

        const division =
          (document.getElementById('division') as HTMLInputElement).value;

        const grado =
          (document.getElementById('grado') as HTMLInputElement).value;

        const turno =
          (document.getElementById('turno') as HTMLInputElement).value;

        const horario =
          (document.getElementById('horario') as HTMLInputElement).value;

        const cupo_maximo =
          (document.getElementById('cupo_maximo') as HTMLInputElement).value;

        const docenteId =
          (document.getElementById('docenteId') as HTMLSelectElement).value;

        if (
          !ciclo_lectivo ||
          !division ||
          !grado ||
          !turno ||
          !horario ||
          !cupo_maximo
        ) {
          Swal.showValidationMessage(
            'Completá todos los campos'
          );

          return false;
        }

        if (Number(cupo_maximo) <= 0) {
          Swal.showValidationMessage(
            'El cupo máximo debe ser mayor a 0'
          );

          return false;
        }

        return {
          ciclo_lectivo,
          division,
          grado,
          turno,
          horario,
          cupo_maximo: Number(cupo_maximo),
          docenteId: docenteId ? Number(docenteId) : null
        };
      }

    }).then((resultado) => {

      if (resultado.isConfirmed) {

        const curso: Curso = resultado.value;

        this.crearCurso(curso);
      }
    });
  }


abrirFormularioEditar(curso: Curso): void {
  Swal.fire({
    title: 'Editar curso',
    html: `
      <input id="ciclo_lectivo" class="swal2-input"
        placeholder="Ciclo lectivo" value="${curso.ciclo_lectivo}">
      <input id="division" class="swal2-input"
        placeholder="División" value="${curso.division}">
      <input id="grado" class="swal2-input"
        placeholder="Grado" value="${curso.grado}">
      <input id="turno" class="swal2-input"
        placeholder="Turno" value="${curso.turno}">
      <input id="horario" class="swal2-input"
        placeholder="Horario" value="${curso.horario ?? ''}">
      <input id="cupo_maximo" class="swal2-input"
        type="number" placeholder="Cupo máximo" value="${curso.cupo_maximo}">
      <select id="docenteId" class="swal2-select">
        ${this.opcionesDocentes(curso.docenteId)}
      </select>
    `,
    showCancelButton: true,
    confirmButtonText: 'Guardar cambios',
    cancelButtonText: 'Cancelar',
    focusConfirm: false,
    preConfirm: () => {
      const ciclo_lectivo =
        (document.getElementById('ciclo_lectivo') as HTMLInputElement).value.trim();
      const division =
        (document.getElementById('division') as HTMLInputElement).value.trim();
      const grado =
        (document.getElementById('grado') as HTMLInputElement).value.trim();
      const turno =
        (document.getElementById('turno') as HTMLInputElement).value.trim();
      const horario =
        (document.getElementById('horario') as HTMLInputElement).value.trim();
      const cupo_maximo =
        (document.getElementById('cupo_maximo') as HTMLInputElement).value.trim();

      const docenteId =
        (document.getElementById('docenteId') as HTMLSelectElement).value;

      if (!ciclo_lectivo || !division || !grado || !turno || !horario || !cupo_maximo) {
        Swal.showValidationMessage('Completá todos los campos');
        return false;
      }

      if (Number(cupo_maximo) <= 0) {
        Swal.showValidationMessage('El cupo máximo debe ser mayor a 0');
        return false;
      }

      return {
        ciclo_lectivo,
        division,
        grado,
        turno,
        horario,
        cupo_maximo: Number(cupo_maximo),
        docenteId: docenteId ? Number(docenteId) : null
      };
    }
  }).then((resultado) => {
    if (resultado.isConfirmed && resultado.value) {
      this.cursoService.modificarCurso(curso.id, resultado.value).subscribe({
        next: () => {
          Swal.fire('¡Modificado!', 'El curso se actualizó correctamente.', 'success');
          this.cargarCursos();
        },
        error: (error) => {
          console.error('Error al modificar curso:', error);
          Swal.fire('Error', 'No se pudo modificar el curso.', 'error');
        }
      });
    }
  });
}

confirmarEliminar(curso: Curso): void {
  Swal.fire({
    title: '¿Eliminar curso?',
    text: `Se eliminará el curso ${curso.grado} ${curso.division}.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#dc2626'
  }).then((resultado) => {
    if (resultado.isConfirmed) {
      this.cursoService.eliminarCurso(curso.id).subscribe({
        next: () => {
          Swal.fire('Eliminado', 'El curso fue eliminado.', 'success');
          this.cargarCursos();
        },
        error: (error) => {
          console.error('Error al eliminar curso:', error);

          const mensaje = error.status === 409
            ? 'No se puede eliminar este curso porque tiene alumnos asociados (o no existe).'
            : 'No se pudo eliminar el curso.';

          Swal.fire('No se pudo eliminar', mensaje, 'error');
        }
      });
    }
  });
}



  crearCurso(curso: Curso): void {

    this.cursoService.crearCurso(curso).subscribe({

      next: () => {

        Swal.fire({
          icon: 'success',
          title: '¡Curso creado!',
          text: 'El curso fue registrado correctamente.',
          confirmButtonText: 'Aceptar'
        });

        this.cargarCursos();
      },

      error: (error) => {

        console.error('Error al crear curso:', error);

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo registrar el curso.'
        });
      }
    });
  }
}