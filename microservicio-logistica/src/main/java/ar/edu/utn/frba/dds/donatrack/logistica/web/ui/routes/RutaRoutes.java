package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers.RutaController;
import io.javalin.config.RoutesConfig;

public class RutaRoutes {

  private RutaRoutes() { }

  public static void registrar(RoutesConfig ruta, RutaController controller) {
    ruta.get("/seguimiento_entregas", controller::verSeguimiento);
  }

}
