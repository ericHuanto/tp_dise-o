package ar.edu.utn.frba.dds.donatrack.donaciones.web.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.AsignacionController;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;

public class AsignacionRoutes {

  private AsignacionRoutes() {}

  public static void registrar(RoutesConfig ruta, AsignacionController controller) {
    ruta.post("/rankings", controller::crearRankings);
    ruta.get("/rankings", controller::obtenerTodos);
    ruta.get("/rankings/{id}", controller::obtener);
    ruta.patch("/rankings/{id}", controller::confirmar);//paso 2 Asignación Realizada
  }

}
