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
    if (repoUsuarios.buscarPorCredenciales(cfg.adminUsuario("admin")) != null) {
      return;
    }
    //Usuarios
    Usuario comedorAmerica = new Usuario("comedorA", "beneficiario");
    Usuario juan = new Usuario("juan", "donante");
    Usuario duenio = new Usuario(cfg.adminUsuario("admin"), cfg.adminPassword("admin"));
    Usuario encargadoDeposito = new Usuario("deposito", "deposito");

    //Acciones
    comedorAmerica.vincularBeneficiario(1L); // ID 1: Comedor america
    comedorAmerica.otorgarPermiso(Permiso.AUTENTICADO);
    comedorAmerica.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
    juan.vincularDonante(1L); // ID 1: Juan Perez
    juan.otorgarPermiso(Permiso.AUTENTICADO);
    juan.otorgarPermiso(Permiso.VER_DONACIONES);
    juan.otorgarPermiso(Permiso.VER_BENEFICIARIOS);
    duenio.otorgarPermiso(Permiso.ADMINISTRADOR);
    encargadoDeposito.otorgarPermiso(Permiso.CREAR_DONACION);

    try {
      beginTransaction();
      repoUsuarios.guardar(duenio);
      repoUsuarios.guardar(encargadoDeposito);
      repoUsuarios.guardar(juan);
      repoUsuarios.guardar(comedorAmerica);
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