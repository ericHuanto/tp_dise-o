package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.CamionController;
import io.javalin.config.RoutesConfig;

public class CamionRoutes {

  private CamionRoutes() { }

  public static void registrar(RoutesConfig ruta, CamionController camionController) {
    ruta.post("/api/camiones", camionController::crear);
    ruta.get("/api//camiones", camionController::obtenerTodos);
    ruta.get("/api/camiones/{patente}", camionController::obtener);
    ruta.patch("/api/camiones/{patente}", camionController::actualizar);
    ruta.delete("/api//camiones/{patente}", camionController::eliminar);
  }

}
