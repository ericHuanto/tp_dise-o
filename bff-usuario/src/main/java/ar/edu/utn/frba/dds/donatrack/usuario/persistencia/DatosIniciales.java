package ar.edu.utn.frba.dds.donatrack.usuario.persistencia;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private final UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    if (repoUsuarios.buscarPorCredenciales("admin") != null) {
      return;
    }

    //Se complementa con el DatosIniciales de Donacion
    Usuario usuarioBeneficiario = new Usuario("comedorA", "beneficiario");
    usuarioBeneficiario.vincularBeneficiario(1L); // ID 1: Comedor america

    Usuario usuarioBeneficiario2 = new Usuario("comedorP", "beneficiario");
    usuarioBeneficiario2.vincularBeneficiario(2L); // ID 2: Comedor Los Pibes

    Usuario usuarioDonante = new Usuario("juan", "donante");
    usuarioDonante.vincularDonante(1L); // ID 1: Juan Perez

    Usuario usuarioDonante2 = new Usuario("juana", "donante");
    usuarioDonante2.vincularDonante(2L); // ID 2: Juana Perez

    Usuario admin = new Usuario("admin", "admin");
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
    }
  }
}