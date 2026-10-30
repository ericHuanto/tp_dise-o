package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.HomeController;
import io.javalin.config.RoutesConfig;

public class HomeRoutes {

  private HomeRoutes() { }

  public static void registrar(RoutesConfig ruta, HomeController controller) {
    ruta.get("/", controller::mostrarHome);
  }

}
