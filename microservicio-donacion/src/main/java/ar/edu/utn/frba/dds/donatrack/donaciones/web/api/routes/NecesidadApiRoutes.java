package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.NecesidadApiController;
import io.javalin.config.RoutesConfig;

public class NecesidadApiRoutes {

  private NecesidadApiRoutes() {}

  public static void registrar(RoutesConfig ruta, NecesidadApiController controller) {
    ruta.post("/api/beneficiarios/{id}/necesidades", controller::crear);
    ruta.get("/api/beneficiarios/{id}/necesidades", controller::obtenerTodos);
    ruta.get("/api/beneficiarios/{id}/necesidades/{nid}", controller::obtener);
    ruta.patch("/api/beneficiarios/{id}/necesidades/{nid}", controller::actualizar);
    ruta.delete("/api/beneficiarios/{id}/necesidades/{nid}", controller::eliminar);
  }

}
