package ar.edu.utn.frba.dds.donatrack.donaciones.web.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.DonanteController;
import io.javalin.config.RoutesConfig;

public class DonanteRoutes {

  private DonanteRoutes() {}

  public static void registrar(RoutesConfig ruta, DonanteController controller) {
    ruta.post("/donantes", controller::crear);
    ruta.get("/donantes", controller::obtenerTodos);
    ruta.get("/donantes/{id}", controller::obtener);
    ruta.patch("/donantes/{id}", controller::actualizar);
    ruta.delete("/donantes/{id}", controller::eliminar);
  }

}
