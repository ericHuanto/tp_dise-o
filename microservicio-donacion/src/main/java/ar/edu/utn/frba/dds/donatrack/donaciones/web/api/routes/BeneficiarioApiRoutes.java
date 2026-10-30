package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.BeneficiarioApiController;
import io.javalin.config.RoutesConfig;

public class BeneficiarioApiRoutes {

  private BeneficiarioApiRoutes() {}

  public static void registrar(RoutesConfig ruta, BeneficiarioApiController controller) {
    ruta.post("/beneficiarios", controller::crear);
    ruta.get("/beneficiarios", controller::obtenerTodos);
    ruta.get("/beneficiarios/{id}", controller::obtener);
    ruta.patch("/beneficiarios/{id}", controller::actualizar);
    ruta.delete("/beneficiarios/{id}", controller::eliminar);
  }

}
