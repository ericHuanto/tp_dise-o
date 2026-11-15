package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class AdminInicial implements WithSimplePersistenceUnit {

  public static void init() {
    new AdminInicial().crearSiNoExiste();
  }

  private void crearSiNoExiste() {
    ConfiguracionEntorno cfg = ConfiguracionEntorno.getInstance();
    String nombre = cfg.adminUsuario("admin");
    String clave = cfg.adminPassword("admin");

    withTransaction(() -> {
      boolean existe = !entityManager()
          .createQuery("SELECT u FROM Usuario u WHERE u.nombreUsuario = :n", Usuario.class)
          .setParameter("n", nombre)
          .getResultList().isEmpty();
      if (existe) return;

      Usuario admin = new Usuario(nombre, clave);   // el constructor hashea
      admin.otorgarPermiso(Permiso.ADMINISTRADOR);
      entityManager().persist(admin);
    });
    WithSimplePersistenceUnit.dispose();
  }
}