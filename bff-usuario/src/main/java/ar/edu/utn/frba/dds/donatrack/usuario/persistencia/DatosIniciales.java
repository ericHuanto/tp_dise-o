package ar.edu.utn.frba.dds.donatrack.usuario.persistencia;

import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private final UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    ConfiguracionEntorno cfg = ConfiguracionEntorno.getInstance();
    String nombreAdmin = cfg.adminUsuario("admin");
    String claveAdmin = cfg.adminPassword("admin");

    if (repoUsuarios.buscarPorCredenciales(nombreAdmin) != null) {
      return;
    }

    //Se complementa con el DatosIniciales de Donacion (los IDs coinciden con los de esa base)
    Usuario usuarioBeneficiario = new Usuario("comedorA", "beneficiario");
    usuarioBeneficiario.vincularBeneficiario(1L); // ID 1: Comedor america

    Usuario usuarioBeneficiario2 = new Usuario("comedorP", "beneficiario");
    usuarioBeneficiario2.vincularBeneficiario(2L); // ID 2: Comedor Los Pibes

    Usuario usuarioDonante = new Usuario("juan", "donante");
    usuarioDonante.vincularDonante(1L); // ID 1: Juan Perez

    Usuario usuarioDonante2 = new Usuario("juana", "donante");
    usuarioDonante2.vincularDonante(2L); // ID 2: Juana Perez

    Usuario admin = new Usuario(nombreAdmin, claveAdmin);
    admin.otorgarPermiso(Permiso.ADMINISTRADOR);

    try {
      beginTransaction();
      repoUsuarios.guardar(admin);
      repoUsuarios.guardar(usuarioDonante);
      repoUsuarios.guardar(usuarioDonante2);
      repoUsuarios.guardar(usuarioBeneficiario);
      repoUsuarios.guardar(usuarioBeneficiario2);
      commitTransaction();
      System.out.println("Usuarios iniciales cargados en BFF con éxito.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar usuarios iniciales en BFF.");
      e.printStackTrace();
    } finally {
      WithSimplePersistenceUnit.dispose();
    }
  }
}