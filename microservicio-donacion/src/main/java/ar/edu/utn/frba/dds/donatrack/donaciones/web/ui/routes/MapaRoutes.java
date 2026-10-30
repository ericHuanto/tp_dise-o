package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.MapaController;
import io.javalin.config.RoutesConfig;

public class MapaRoutes {

  private MapaRoutes() { }

  public static void registrar(RoutesConfig ruta, MapaController controller) {
    ruta.get("/donaciones/entregas/galeria", controller::mostrarDonacionesEntregadas);
  }

}
