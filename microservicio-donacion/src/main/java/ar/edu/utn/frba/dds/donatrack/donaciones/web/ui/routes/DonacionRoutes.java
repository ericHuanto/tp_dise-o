package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.DonacionController;
import io.javalin.config.RoutesConfig;

public class DonacionRoutes {

  public DonacionRoutes() { }

  public static void registrar(RoutesConfig ruta, DonacionController controller) {
    ruta.get("/donaciones", controller::mostrarDonaciones);
    ruta.post("/donaciones/{id}/vencido", controller::marcarComoVencida);
  }

}
