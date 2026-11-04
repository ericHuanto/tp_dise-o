package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.PerfilController;
import io.javalin.config.RoutesConfig;

public class PerfilRoutes {

  private PerfilRoutes() { }

  public static void registrar(RoutesConfig ruta, PerfilController controller) {
    ruta.get("/perfil/eleccion", controller::mostrarOpcionesPerfil);
    ruta.get("/perfil/beneficiario", controller::mostrarFormularioRegistroBeneficiario);
    ruta.post("/perfil/beneficiario", controller::guardarBeneficiario);
    ruta.get("/perfil/donante-humana", controller::mostrarFormularioRegistroDonanteHumano);
    ruta.post("/perfil/donante-humana", controller::guardarDonanteHumana);
  }

}
