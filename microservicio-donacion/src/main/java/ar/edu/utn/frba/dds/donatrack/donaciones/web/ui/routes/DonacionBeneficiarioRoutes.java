package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.DonacionBeneficiarioController;
import io.javalin.config.RoutesConfig;

public class DonacionBeneficiarioRoutes {

  private DonacionBeneficiarioRoutes() {}

  public static void registrar(RoutesConfig ruta, DonacionBeneficiarioController controller) {
    ruta.get("/mis-donaciones", controller::listar);
    ruta.get("/mis-donaciones/{id}", controller::verDetalle);
  }
}
