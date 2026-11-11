package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers.MapaController;
import io.javalin.config.RoutesConfig;

public class MapaRoutes {

  private MapaRoutes() { }

  public static void registrar(RoutesConfig ruta, MapaController controller) {
    ruta.get("/beneficiarios/donaciones/recibidas", controller::mostrarDonacionesRecibidas);
  }

}
