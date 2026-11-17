package ar.edu.utn.frba.dds.donatrack.logistica.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers.EntregaApiController;
import io.javalin.config.RoutesConfig;

public class EntregaApiRoutes {

  private EntregaApiRoutes() {}

  public static void registrar(RoutesConfig ruta, EntregaApiController controller) {
    ruta.get("/api/entregas", controller::obtenerTodos);
    ruta.get("/api/entregas/{id}", controller::obtener);
    ruta.patch("/api/entregas/{id}/foto", controller::agregarFoto);
    ruta.patch("/api/entregas/{id}/recibida", controller::confirmarRecibida); // activa el paso 5 de la donación (donación entregada).
    ruta.patch("/api/entregas/{id}/no-recibida", controller::confirmarNoRecibida); // activa el paso 5B de la donación (error al entregar la donación).
    ruta.patch("/api/entregas/{id}/reingreso", controller::reingresarADeposito); // activa el paso 6 de la donación (donación devuelta a depósito).
  }

}
