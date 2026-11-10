package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.CsvController;
import io.javalin.config.RoutesConfig;

public class CsvRoutes {

  private CsvRoutes() { }

  public static void registrar(RoutesConfig ruta, CsvController controller) {
    ruta.get("/donantes/importacion", controller::mostrarFormularioCreacion);
    ruta.post("/donantes/importacion", controller::procesarDonantesAntiguos);
  }

}
