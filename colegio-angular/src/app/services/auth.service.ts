import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

interface LoginResponse {
  token: string;
}

interface JwtPayload {
  sub: string;
  role: string;
  personalId?: number | null;
  exp: number;
  iat: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8081/auth';

  private platformId = inject(PLATFORM_ID);

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      { username, password }
    );
  }

guardarToken(token: string): void {
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem('token', token);
  }
}

obtenerToken(): string | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }

    return localStorage.getItem('token');
  }

  obtenerRol(): string | null {
    const token = this.obtenerToken();

    if (!token) {
      return null;
    }

    const payload = jwtDecode<JwtPayload>(token);

    return payload.role;
  }

  // Devuelve true si el rol del usuario esta entre los permitidos.
  tieneRol(...roles: string[]): boolean {
    const rol = this.obtenerRol();
    return rol !== null && roles.includes(rol);
  }

  // Id de la persona (Personal) vinculada a la cuenta, si la tiene.
  obtenerPersonalId(): number | null {
    const token = this.obtenerToken();

    if (!token) {
      return null;
    }

    const payload = jwtDecode<JwtPayload>(token);

    return payload.personalId ?? null;
  }

  obtenerUsuario(): string | null {
    const token = this.obtenerToken();

    if (!token) {
      return null;
    }

    const payload = jwtDecode<JwtPayload>(token);

    return payload.sub;
  }

logout(): void {
  if (typeof localStorage !== 'undefined') {
    localStorage.removeItem('token');
  }
}

  estaLogueado(): boolean {
    return this.obtenerToken() !== null;
  }
}