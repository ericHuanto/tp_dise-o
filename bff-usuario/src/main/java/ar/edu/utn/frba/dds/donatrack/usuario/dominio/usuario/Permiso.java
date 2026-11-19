package ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario;

import io.javalin.security.RouteRole;

public enum Permiso implements RouteRole {
  //Autenticado
  AUTENTICADO,
  //Donante
  VER_DONACIONES,
  //Beneficiario
  SOLICITAR_AYUDA,
  //admin
  CREAR_DONACION,
  EDITAR_DONACION,
  GESTIONAR_USUARIOS,
  GESTIONAR_CAMIONES,
  GESTIONAR_ENTREGAS,
  ADMINISTRADOR
}
