package ar.edu.utn.frba.dds.donatrack.logistica.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers.CamionApiController;
import io.javalin.config.RoutesConfig;

public class CamionApiRoutes {

  private CamionApiRoutes() { }

  public static void registrar(RoutesConfig ruta, CamionApiController controller) {
    ruta.post("/api/camiones", controller::crear);
    ruta.get("/api//camiones", controller::obtenerTodos);
    ruta.get("/api/camiones/{patente}", controller::obtener);
    ruta.patch("/api/camiones/{patente}", controller::actualizar);
    ruta.delete("/api//camiones/{patente}", controller::eliminar);
  }

}
