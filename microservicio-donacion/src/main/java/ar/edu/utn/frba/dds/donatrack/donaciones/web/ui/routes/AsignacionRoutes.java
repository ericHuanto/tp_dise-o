package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers.AsignacionController;
import io.javalin.config.RoutesConfig;

public class AsignacionRoutes {

  private AsignacionRoutes() { }

  public static void registrar(RoutesConfig ruta, AsignacionController controller) {
    ruta.get("/rankings", controller::mostrarTodos);
    ruta.post("/rankings/{id}", controller::confirmarAsignacion);
  }

}
