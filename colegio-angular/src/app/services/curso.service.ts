import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Curso } from '../models/curso';

@Injectable({
  providedIn: 'root'
})
export class CursoService {

  private apiUrl = 'http://localhost:8081/cursos';

  constructor(private http: HttpClient) {}

  obtenerCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(this.apiUrl);
  }

  obtenerCursosPorDocente(docenteId: number): Observable<Curso[]> {
    return this.http.get<Curso[]>(`${this.apiUrl}/docente/${docenteId}`);
  }

  crearCurso(curso: Curso): Observable<Curso> {
    return this.http.post<Curso>(this.apiUrl, curso);
  }
  modificarCurso(id: number, curso: Curso): Observable<Curso> {
    return this.http.put<Curso>(`${this.apiUrl}/${id}`, curso);
  }

  eliminarCurso(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  inscribirAlumno(cursoId: number, alumnoId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${cursoId}/alumnos/${alumnoId}`, {});
  }

  quitarAlumno(cursoId: number, alumnoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${cursoId}/alumnos/${alumnoId}`);
  }

}