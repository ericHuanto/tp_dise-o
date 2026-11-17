package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorhash;

import org.mindrot.jbcrypt.BCrypt;

public class Integridad {
  public static String hashear(String contraseñaPlana) {
    return BCrypt.hashpw(contraseñaPlana, BCrypt.gensalt());
  }

  public static boolean validar(String contraseñaPlana, String contraseñaHasheada) {
    return BCrypt.checkpw(contraseñaPlana, contraseñaHasheada);
  }

}
