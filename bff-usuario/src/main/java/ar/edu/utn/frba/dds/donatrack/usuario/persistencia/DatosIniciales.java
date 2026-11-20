package ar.edu.utn.frba.dds.donatrack.usuario.persistencia;

import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private final UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();
  private final ConfiguracionEntorno cfg = ConfiguracionEntorno.getInstance();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    //Usuarios
    Usuario comedorAmerica = new Usuario("comedorA", "beneficiario");
    Usuario comedorPibes = new Usuario("comedorP", "beneficiario");
    Usuario juan = new Usuario("juan", "donante");
    Usuario juana = new Usuario("juana", "donante");
    Usuario duenio = new Usuario(cfg.adminUsuario("admin"), cfg.adminPassword("admin"));
    Usuario encargadoDeposito = new Usuario("deposito", "deposito");

    //Acciones
    comedorAmerica.vincularBeneficiario(1L); // ID 1: Comedor america
    comedorPibes.vincularBeneficiario(2L); // ID 2: Comedor Los Pibes
    juan.vincularDonante(1L); // ID 1: Juan Perez
    juana.vincularDonante(2L); // ID 2: Juana Perez
    comedorAmerica.otorgarPermiso(Permiso.AUTENTICADO);
    comedorPibes.otorgarPermiso(Permiso.AUTENTICADO);
    juan.otorgarPermiso(Permiso.AUTENTICADO);
    juana.otorgarPermiso(Permiso.AUTENTICADO);
    duenio.otorgarPermiso(Permiso.ADMINISTRADOR);
    encargadoDeposito.otorgarPermiso(Permiso.CREAR_DONACION);

    try {
      beginTransaction();
      repoUsuarios.guardar(duenio);
      repoUsuarios.guardar(encargadoDeposito);
      repoUsuarios.guardar(juan);
      repoUsuarios.guardar(juana);
      repoUsuarios.guardar(comedorAmerica);
      repoUsuarios.guardar(comedorPibes);
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