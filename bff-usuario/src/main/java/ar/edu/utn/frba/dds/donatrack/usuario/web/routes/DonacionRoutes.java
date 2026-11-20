package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionController;
import io.javalin.config.RoutesConfig;

public class DonacionRoutes {

  public DonacionRoutes() { }

  public static void registrar(RoutesConfig ruta, DonacionController controller) {
    ruta.get("/donaciones", controller::mostrarDonaciones, Permiso.VER_DONACIONES);
    ruta.post("/donaciones/{id}/vencido", controller::marcarComoVencida, Permiso.EDITAR_DONACION);
  }

}
