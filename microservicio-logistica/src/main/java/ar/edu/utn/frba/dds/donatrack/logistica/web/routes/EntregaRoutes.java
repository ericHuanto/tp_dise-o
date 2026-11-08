package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.EntregaController;
import io.javalin.config.RoutesConfig;

public class EntregaRoutes {

  private EntregaRoutes() {}

  public static void registrar(RoutesConfig ruta, EntregaController entregaController) {
    ruta.get("/api/entregas", entregaController::obtenerTodos);
    ruta.get("/api/entregas/{id}", entregaController::obtener);
    ruta.patch("/api/entregas/{id}/foto", entregaController::agregarFoto);
    ruta.patch("/api/entregas/{id}/recibida", entregaController::confirmarRecibida); // activa el paso 5 de la donación (donación entregada).
    ruta.patch("/api/entregas/{id}/no-recibida", entregaController::confirmarNoRecibida); // activa el paso 5B de la donación (error al entregar la donación).
    ruta.patch("/api/entregas/{id}/reingreso", entregaController::reingresarADeposito); // activa el paso 6 de la donación (donación devuelta a depósito).
    //después cambiarlo a la nueva carpeta
    ruta.get("/mis-entregas", entregaController::mostrarMisEntregas);
    ruta.get("/mis-entregas/{id}/confirmar", entregaController::mostrarFormularioConfirmacion);
    ruta.post("/mis-entregas/{id}/confirmar", entregaController::confirmarRecepcionDesdeVista);
  }

}
