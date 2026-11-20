package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionBeneficiarioController;
import io.javalin.config.RoutesConfig;

public class DonacionBeneficiarioRoutes {

  private DonacionBeneficiarioRoutes() {}

  public static void registrar(RoutesConfig ruta, DonacionBeneficiarioController controller) {
    ruta.get("/mis-donaciones", controller::listar, Permiso.SOLICITAR_AYUDA);
    ruta.get("/mis-donaciones/{id}", controller::verDetalle, Permiso.SOLICITAR_AYUDA);
    ruta.get("/notificaciones", controller::mostrarNovedades, Permiso.SOLICITAR_AYUDA);
  }
}