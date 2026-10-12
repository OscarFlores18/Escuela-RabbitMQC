export interface Curso {
  id: number;
  ciclo_lectivo: string;
  division: string;
  grado: string;
  turno: string;
  materia: string;
  horario: string;
  cupo_maximo: number;
  cantidadAlumnos?: number;
  docenteId?: number;
  alumnoIds?: number[];
}
