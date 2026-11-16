package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.EntregaController;
import io.javalin.config.RoutesConfig;

public class EntregaRoutes {

  private EntregaRoutes() { }

  public static void registrar(RoutesConfig ruta, EntregaController controller) {
    ruta.get("/mis-entregas", controller::mostrarMisEntregas);
    ruta.get("/mis-entregas/{id}/confirmar", controller::mostrarFormularioConfirmacion);
    ruta.post("/mis-entregas/{id}/confirmar", controller::confirmarRecepcion);
  }
}