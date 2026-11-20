package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonanteController;
import io.javalin.config.RoutesConfig;

public class DonanteRoutes {

  private DonanteRoutes() { }

  public static void registrar(RoutesConfig ruta, DonanteController controller) {
    ruta.get("/donante/donaciones", controller::donacionesPropias, Permiso.VER_DONACIONES);
    ruta.get("/donante/beneficiarios", controller::beneficiarios, Permiso.VER_BENEFICIARIOS);
    ruta.get("/donante/beneficiarios/{id}", controller::detalleBeneficiario, Permiso.VER_BENEFICIARIOS);
  }

}
