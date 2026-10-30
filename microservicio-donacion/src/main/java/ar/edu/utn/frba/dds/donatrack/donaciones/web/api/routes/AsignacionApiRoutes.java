package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.AsignacionApiController;
import io.javalin.config.RoutesConfig;

public class AsignacionApiRoutes {

  private AsignacionApiRoutes() {}

  public static void registrar(RoutesConfig ruta, AsignacionApiController controller) {
    ruta.post("/rankings", controller::crearRankings);
    ruta.get("/rankings", controller::obtenerTodos);
    ruta.get("/rankings/{id}", controller::obtener);
    ruta.patch("/rankings/{id}", controller::confirmar);//paso 2 Asignación Realizada
  }

}
