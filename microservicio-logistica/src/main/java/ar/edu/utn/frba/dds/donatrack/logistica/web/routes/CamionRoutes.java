package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.CamionController;
import io.javalin.config.RoutesConfig;

public class CamionRoutes {

  private CamionRoutes() { }

  public static void registrar(RoutesConfig ruta, CamionController camionController) {
    ruta.post("/camiones", camionController::crear);
    ruta.get("/camiones", camionController::obtenerTodos);
    ruta.get("/camiones/{patente}", camionController::obtener);
    ruta.patch("/camiones/{patente}", camionController::actualizar);
    ruta.delete("/camiones/{patente}", camionController::eliminar);
  }

}
