package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.RutaController;
import io.javalin.config.RoutesConfig;

public class RutaRoutes {

  private RutaRoutes() { }

  public static void registrar(RoutesConfig ruta, RutaController controller) {
    ruta.get("/seguimiento_entregas", controller::verSeguimiento);
  }
}