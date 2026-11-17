package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.RegistroEntregaController;
import io.javalin.config.RoutesConfig;

public class RegistroEntregaRoutes {
  private RegistroEntregaRoutes() {}

  public static void registrar(RoutesConfig ruta, RegistroEntregaController controller) {
    ruta.get("/registros-entrega/nueva", controller::mostrarFormulario);
  }
}