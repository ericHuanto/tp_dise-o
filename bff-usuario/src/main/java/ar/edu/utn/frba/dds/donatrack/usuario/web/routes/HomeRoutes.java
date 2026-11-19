package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.HomeController;
import io.javalin.config.RoutesConfig;

public class HomeRoutes {

  private HomeRoutes() { }

  public static void registrar(RoutesConfig ruta, HomeController controller) {
    ruta.get("/", controller::mostrarLanding); // Pública
    ruta.get("/home", controller::mostrarHome, Permiso.AUTENTICADO);
  }
}