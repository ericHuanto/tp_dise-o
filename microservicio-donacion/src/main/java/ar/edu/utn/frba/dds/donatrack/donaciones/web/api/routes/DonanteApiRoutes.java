package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.DonanteApiController;
import io.javalin.config.RoutesConfig;

public class DonanteApiRoutes {

  private DonanteApiRoutes() {}

  public static void registrar(RoutesConfig ruta, DonanteApiController controller) {
    ruta.post("/api/donantes", controller::crear);
    ruta.get("/api/donantes", controller::obtenerTodos);
    ruta.get("/api/donantes/{id}", controller::obtener);
    ruta.patch("/api/donantes/{id}", controller::actualizar);
    ruta.delete("/api/donantes/{id}", controller::eliminar);
  }

}
