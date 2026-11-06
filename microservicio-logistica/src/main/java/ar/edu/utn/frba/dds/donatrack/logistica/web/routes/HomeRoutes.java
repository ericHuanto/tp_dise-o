package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.HomeController;
import io.javalin.config.RoutesConfig;

public class HomeRoutes {

  public static void registrar(RoutesConfig ruta, HomeController controller) {
    ruta.get("/", controller::mostrarHome);
  }

}
