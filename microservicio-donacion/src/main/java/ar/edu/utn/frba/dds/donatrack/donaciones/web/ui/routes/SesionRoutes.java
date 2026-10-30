package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.SesionController;
import io.javalin.config.RoutesConfig;

public class SesionRoutes {

  private SesionRoutes() { }

  public static void registrar(RoutesConfig ruta, SesionController controller) {
    ruta.get("/registro", controller::mostrarFormularioRegistro);
    ruta.get("/login", controller::mostrarFormularioInicioSesion);
    ruta.post("/registro", controller::iniciarRegistro);
    ruta.post("/login", controller::iniciarSesion);
    ruta.post("/logout", controller::cerrarSesion);
  }

}
