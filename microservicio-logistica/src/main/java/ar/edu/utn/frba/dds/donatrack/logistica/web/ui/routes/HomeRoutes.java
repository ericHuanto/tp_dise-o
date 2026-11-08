package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers.HomeController;
import io.javalin.config.RoutesConfig;

public class HomeRoutes {

  public static void registrar(RoutesConfig ruta, HomeController controller) {
    ruta.get("/", controller::mostrarHome);
  }

}
