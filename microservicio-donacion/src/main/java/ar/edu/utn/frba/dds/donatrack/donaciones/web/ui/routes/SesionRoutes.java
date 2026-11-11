package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers.SesionController;
import io.javalin.config.RoutesConfig;

public class SesionRoutes {

  private SesionRoutes() { }

  public static void registrar(RoutesConfig ruta, SesionController controller) {
    ruta.get("/login", controller::mostrarFormularioInicioSesion);
    ruta.post("/login", controller::iniciarSesion);
    ruta.post("/logout", controller::cerrarSesion);
  }

}
