package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MapaController;
import io.javalin.config.RoutesConfig;

public class MapaRoutes {

  private MapaRoutes() { }

  public static void registrar(RoutesConfig ruta, MapaController controller) {
    ruta.get("/beneficiarios/donaciones/recibidas", controller::mostrarDonacionesRecibidas, Permiso.AUTENTICADO);
  }
}