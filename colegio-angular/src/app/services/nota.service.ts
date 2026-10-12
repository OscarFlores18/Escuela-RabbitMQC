import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Nota } from '../models/nota';

@Injectable({
  providedIn: 'root'
})
export class NotaService {

  private apiUrl = 'http://localhost:8081/notas';

  constructor(private http: HttpClient) {}

  obtenerNotas(): Observable<Nota[]> {
    return this.http.get<Nota[]>(this.apiUrl);
  }

  obtenerPorAlumno(alumnoId: number): Observable<Nota[]> {
    return this.http.get<Nota[]>(`${this.apiUrl}/alumno/${alumnoId}`);
  }

  crearNota(nota: Nota): Observable<Nota> {
    return this.http.post<Nota>(this.apiUrl, nota);
  }

  modificarNota(id: number, nota: Nota): Observable<Nota> {
    return this.http.put<Nota>(`${this.apiUrl}/${id}`, nota);
  }

  eliminarNota(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}
