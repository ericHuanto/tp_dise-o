package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.PlanificacionController;
import io.javalin.config.RoutesConfig;

public class PlanificacionRoutes {
  private PlanificacionRoutes() {}

  public static void registrar(RoutesConfig ruta, PlanificacionController planificacionController) {
    ruta.post("/api/planificaciones", planificacionController::planificar);
    ruta.post("/api/planificaciones/callback-externo", planificacionController::callback);// webhook del proveedor
    ruta.post("/api/planificaciones/manual", planificacionController::ejecutarManual);      // disparo manual del cron
  }
}