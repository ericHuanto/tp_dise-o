package ar.edu.utn.frba.dds.donatrack.donaciones.web.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.NecesidadController;
import io.javalin.config.RoutesConfig;

public class NecesidadRoutes {

  private NecesidadRoutes() {}

  public static void registrar(RoutesConfig ruta, NecesidadController controller) {
    ruta.post("/beneficiarios/{id}/necesidades", controller::crear);
    ruta.get("/beneficiarios/{id}/necesidades", controller::obtenerTodos);
    ruta.get("/beneficiarios/{id}/necesidades/{nid}", controller::obtener);
    ruta.patch("/beneficiarios/{id}/necesidades/{nid}", controller::actualizar);
    ruta.delete("/beneficiarios/{id}/necesidades/{nid}", controller::eliminar);
  }

}
