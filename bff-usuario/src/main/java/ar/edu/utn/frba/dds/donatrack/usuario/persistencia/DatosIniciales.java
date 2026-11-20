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

    // Usuarios existentes
    Usuario comedorAmerica = new Usuario("comedorA", "beneficiario");
    Usuario juan = new Usuario("juan", "donante");
    Usuario duenio = new Usuario(cfg.adminUsuario("admin"), cfg.adminPassword("admin"));

    // Usuarios adicionales para probar todos los camiones y rutas
    Usuario juana = new Usuario("juana", "donante");
    Usuario comedorPibes = new Usuario("comedorP", "beneficiario");
    Usuario hogarSantaRita = new Usuario("santarita", "beneficiario");

    // Permisos y vínculos con IDs de Donaciones
    comedorAmerica.vincularBeneficiario(1L); // ID 1: Comedor america
    comedorAmerica.otorgarPermiso(Permiso.AUTENTICADO);
    comedorAmerica.otorgarPermiso(Permiso.SOLICITAR_AYUDA);

    comedorPibes.vincularBeneficiario(2L); // ID 2: Comedor Los Pibes
    comedorPibes.otorgarPermiso(Permiso.AUTENTICADO);
    comedorPibes.otorgarPermiso(Permiso.SOLICITAR_AYUDA);

    hogarSantaRita.vincularBeneficiario(3L); // ID 3: Hogar Santa Rita
    hogarSantaRita.otorgarPermiso(Permiso.AUTENTICADO);
    hogarSantaRita.otorgarPermiso(Permiso.SOLICITAR_AYUDA);

    juan.vincularDonante(1L); // ID 1: Juan Perez
    juan.otorgarPermiso(Permiso.AUTENTICADO);
    juan.otorgarPermiso(Permiso.VER_DONACIONES);
    juan.otorgarPermiso(Permiso.VER_BENEFICIARIOS);

    juana.vincularDonante(2L); // ID 2: Juana Perez
    juana.otorgarPermiso(Permiso.AUTENTICADO);
    juana.otorgarPermiso(Permiso.VER_DONACIONES);
    juana.otorgarPermiso(Permiso.VER_BENEFICIARIOS);

    duenio.otorgarPermiso(Permiso.ADMINISTRADOR);

    try {
      beginTransaction();
      repoUsuarios.guardar(duenio);
      repoUsuarios.guardar(juan);
      repoUsuarios.guardar(juana);
      repoUsuarios.guardar(comedorAmerica);
      repoUsuarios.guardar(comedorPibes);
      repoUsuarios.guardar(hogarSantaRita);
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