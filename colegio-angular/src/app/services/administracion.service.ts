import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Personal } from '../models/personal';

@Injectable({
  providedIn: 'root'
})
export class AdministracionService {

  private apiUrl = 'http://localhost:8081/personal';

  constructor(private http: HttpClient) {}

  obtenerPersonal(): Observable<Personal[]> {
    return this.http.get<Personal[]>(this.apiUrl);
  }

  // Listado basico de personal: disponible para cualquier usuario interno
  // (se usa para mostrar/asignar docentes en los cursos).
  obtenerPersonalBasico(): Observable<Personal[]> {
    return this.http.get<Personal[]>(`${this.apiUrl}/basico`);
  }

  crearPersonal(personal: Personal): Observable<Personal> {
    return this.http.post<Personal>(this.apiUrl, personal);
  }

  modificarPersonal(id: number, personal: Personal): Observable<Personal> {
    return this.http.put<Personal>(`${this.apiUrl}/${id}`, personal);
  }

  eliminarPersonal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}