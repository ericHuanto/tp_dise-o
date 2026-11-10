package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.BeneficiarioApiController;
import io.javalin.config.RoutesConfig;

public class BeneficiarioApiRoutes {

  private BeneficiarioApiRoutes() {}

  public static void registrar(RoutesConfig ruta, BeneficiarioApiController controller) {
    ruta.post("/api/beneficiarios", controller::crear);
    ruta.get("/api/beneficiarios", controller::obtenerTodos);
    ruta.get("/api/beneficiarios/{id}", controller::obtener);
    ruta.patch("/api/beneficiarios/{id}", controller::actualizar);
    ruta.delete("/api/beneficiarios/{id}", controller::eliminar);
  }

}
