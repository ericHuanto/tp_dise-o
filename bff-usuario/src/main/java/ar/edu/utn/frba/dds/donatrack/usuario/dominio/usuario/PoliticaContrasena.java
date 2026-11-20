package ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario;

public final class PoliticaContrasena {
  private PoliticaContrasena() { }

  // Devuelve el mensaje de error, o null si la clave cumple la política.
  public static String validar(String clave) {
    if (clave == null || clave.length() < 8) return "La contraseña debe tener al menos 8 caracteres";
    if (clave.length() > 72) return "La contraseña no puede superar los 72 caracteres";
    if (clave.chars().noneMatch(Character::isDigit)) return "La contraseña debe incluir al menos un número";
    return null;
  }
}