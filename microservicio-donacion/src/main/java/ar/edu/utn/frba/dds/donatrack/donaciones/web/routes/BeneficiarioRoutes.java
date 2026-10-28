package ar.edu.utn.frba.dds.donatrack.donaciones.web.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.BeneficiarioController;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;

public class BeneficiarioRoutes {

  private BeneficiarioRoutes() {}

  public static void registrar(RoutesConfig ruta, BeneficiarioController controller) {
    ruta.post("/beneficiarios", controller::crear);
    ruta.get("/beneficiarios", controller::obtenerTodos);
    ruta.get("/beneficiarios/{id}", controller::obtener);
    ruta.patch("/beneficiarios/{id}", controller::actualizar);
    ruta.delete("/beneficiarios/{id}", controller::eliminar);
  }

}
