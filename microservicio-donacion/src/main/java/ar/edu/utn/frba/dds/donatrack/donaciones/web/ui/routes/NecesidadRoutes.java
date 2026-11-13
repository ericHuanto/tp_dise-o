package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers.NecesidadController;
import io.javalin.config.RoutesConfig;

public class NecesidadRoutes {

  private NecesidadRoutes() { }

  public static void registrar(RoutesConfig ruta, NecesidadController controller) {
    ruta.get("/mis-necesidades", controller::listar);
    ruta.get("/mis-necesidades/nueva", controller::mostrarFormulario);
    ruta.post("/mis-necesidades", controller::guardar);
    ruta.get("/mis-necesidades/{id}/editar", controller::mostrarFormularioEdicion);
    ruta.post("/mis-necesidades/{id}/editar", controller::actualizar);
  }
}
