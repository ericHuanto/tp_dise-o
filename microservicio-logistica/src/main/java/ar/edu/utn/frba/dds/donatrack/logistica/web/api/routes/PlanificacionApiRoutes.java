package ar.edu.utn.frba.dds.donatrack.logistica.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers.PlanificacionApiController;
import io.javalin.config.RoutesConfig;

public class PlanificacionApiRoutes {
  private PlanificacionApiRoutes() {}

  public static void registrar(RoutesConfig ruta, PlanificacionApiController controller) {
    ruta.post("/api/planificaciones", controller::planificar);
    ruta.post("/api/planificaciones/callback-externo", controller::callback);// webhook del proveedor
    ruta.post("/api/planificaciones/manual", controller::ejecutarManual);      // disparo manual del cron
  }
}