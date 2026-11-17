package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonanteController;
import io.javalin.config.RoutesConfig;

public class DonanteRoutes {

  private DonanteRoutes() { }

  public static void registrar(RoutesConfig ruta, DonanteController controller) {
    ruta.get("/donante/donaciones", controller::donacionesPropias);
    ruta.get("/donante/beneficiarios", controller::beneficiarios);
    ruta.get("/donante/beneficiarios/{id}", controller::detalleBeneficiario);
  }

}
