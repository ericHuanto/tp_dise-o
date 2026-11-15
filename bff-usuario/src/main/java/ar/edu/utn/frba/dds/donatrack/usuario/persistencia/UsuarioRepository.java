package ar.edu.utn.frba.dds.donatrack.usuario.persistencia;

import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BaseDatoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RegistroNoEncontradoException;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class UsuarioRepository implements WithSimplePersistenceUnit {
  private static final UsuarioRepository INSTANCE = new UsuarioRepository();

  private UsuarioRepository() { }

  public static UsuarioRepository getInstancia() {
    return INSTANCE;
  }

  public void guardar(Usuario usuario) {
    if (usuario.getId() != null) {
      throw new BaseDatoException("No se puede guardar: El usuario ya tiene un ID asignado (ya existe). Debes usar el método actualizar().");
    }
    entityManager().persist(usuario);
  }

  public Usuario buscarPorId(Long id) {
    return entityManager().find(Usuario.class, id);
  }

  public Usuario buscarPorCredenciales(String nombreUsuario) {
    return entityManager().createQuery("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombre", Usuario.class)
        .setParameter("nombre", nombreUsuario)
        .getResultStream().findFirst().orElse(null);
  }

  public void actualizar(Usuario usuario) {
    if (usuario.getId() == null || buscarPorId(usuario.getId()) == null) {
      throw new RegistroNoEncontradoException("No se puede actualizar: no existe en la base de datos el usuario: " + usuario.getId());
    }
    entityManager().merge(usuario);
  }
}