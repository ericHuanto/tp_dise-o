package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.RutaController;
import io.javalin.config.RoutesConfig;

public class RutaRoutes {

  private RutaRoutes() {}

  public static void registrar(RoutesConfig ruta, RutaController rutaController) {
    ruta.get("/rutas", rutaController::obtenerTodas);
    ruta.get("/rutas/{id}", rutaController::obtener);
    ruta.patch("/rutas/{id}/chofer", rutaController::asignarChofer);
    ruta.patch("/rutas/{id}/inicio", rutaController::iniciar); //activa el paso 4 de la donación (En Traslado)
  }

}