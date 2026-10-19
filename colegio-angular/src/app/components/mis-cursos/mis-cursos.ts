import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CursoService } from '../../services/curso.service';
import { AlumnoService } from '../../services/alumno.service';
import { AuthService } from '../../services/auth.service';
import { Curso } from '../../models/curso';
import { Alumno } from '../../models/alumno';

@Component({
  selector: 'app-mis-cursos',
  standalone: true,
  templateUrl: './mis-cursos.html',
  styleUrl: './mis-cursos.css'
})
export class MisCursos implements OnInit {
  cursos: Curso[] = [];
  cargando = true;
  sinVinculo = false;
  private alumnosPorId = new Map<number, Alumno>();

  constructor(
    private cursoService: CursoService,
    private alumnoService: AlumnoService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const personalId = this.authService.obtenerPersonalId();

    if (personalId == null) {
      this.sinVinculo = true;
      this.cargando = false;
      this.cdr.detectChanges();
      return;
    }

    // Cargamos todos los alumnos para mapear ids a nombres
    this.alumnoService.obtenerAlumnos().subscribe({
      next: (alumnos) => {
        this.alumnosPorId = new Map(alumnos.map((a) => [a.id, a]));
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener alumnos:', error);
      }
    });

    // Cargamos los cursos asignados al docente
    this.cursoService.obtenerCursosPorDocente(personalId).subscribe({
      next: (cursos) => {
        this.cursos = cursos;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener cursos del docente:', error);
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  alumnosDe(curso: Curso): Alumno[] {
    return (curso.alumnoIds ?? [])
      .map((id) => this.alumnosPorId.get(id))
      .filter((a): a is Alumno => a !== undefined);
  }

  etiquetaCurso(curso: Curso): string {
    return `${curso.grado} ${curso.division} - ${curso.ciclo_lectivo} (${curso.turno})`;
  }
}
