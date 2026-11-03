package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class UsuarioRepository implements WithSimplePersistenceUnit {
  private static final UsuarioRepository INSTANCE = new UsuarioRepository();

  private UsuarioRepository() { }

  public static UsuarioRepository getInstancia() {
    return INSTANCE;
  }

  public void guardar(Usuario usuario) {
    entityManager().persist(usuario);
  }

  public Usuario buscarPorCredenciales(String nombreUsuario) {
    return entityManager().createQuery("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombre", Usuario.class)
        .setParameter("nombre", nombreUsuario)
        .getResultStream().findFirst().orElse(null);
  }

}
