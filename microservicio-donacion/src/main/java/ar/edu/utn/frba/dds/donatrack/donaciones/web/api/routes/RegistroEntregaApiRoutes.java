package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.RegistroEntregaApiController;
import io.javalin.config.RoutesConfig;

public class RegistroEntregaApiRoutes {
  private RegistroEntregaApiRoutes() {}

  public static void registrar(RoutesConfig ruta, RegistroEntregaApiController controller) {
    ruta.post("/api/registros-entrega", controller::crear);
  }
}