package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CsvController;
import io.javalin.config.RoutesConfig;

public class CsvRoutes {

  private CsvRoutes() { }

  public static void registrar(RoutesConfig ruta, CsvController controller) {
    ruta.get("/donantes/importacion", controller::mostrarFormularioCreacion, Permiso.GESTIONAR_USUARIOS);
    ruta.post("/donantes/importacion", controller::procesarDonantesAntiguos, Permiso.GESTIONAR_USUARIOS);

    ruta.get("/donantes/importacion/estado/{id_tarea}", controller::mostrarPantallaEspera, Permiso.GESTIONAR_USUARIOS);
    ruta.get("/api/donantes/importacion/estado/{id_tarea}", controller::consultarEstadoProceso, Permiso.GESTIONAR_USUARIOS);
    ruta.get("/donantes/importacion/resultado/{id_tarea}", controller::mostrarResultados, Permiso.GESTIONAR_USUARIOS);
  }
}