package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.NecesidadApiController;
import io.javalin.config.RoutesConfig;

public class NecesidadApiRoutes {

  private NecesidadApiRoutes() {}

  public static void registrar(RoutesConfig ruta, NecesidadApiController controller) {
    ruta.post("/beneficiarios/{id}/necesidades", controller::crear);
    ruta.get("/beneficiarios/{id}/necesidades", controller::obtenerTodos);
    ruta.get("/beneficiarios/{id}/necesidades/{nid}", controller::obtener);
    ruta.patch("/beneficiarios/{id}/necesidades/{nid}", controller::actualizar);
    ruta.delete("/beneficiarios/{id}/necesidades/{nid}", controller::eliminar);
  }

}
