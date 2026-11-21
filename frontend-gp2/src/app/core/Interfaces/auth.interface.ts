export interface Account {
  usuario: string;
  rol: 'ADMIN' | 'CLIENTE';
  idPersona: number;
}

export interface Registration {
  nombre: string;
  email: string;
  telefono: string;
  direccion: string;
  usuario: string;
  password: string;
}
