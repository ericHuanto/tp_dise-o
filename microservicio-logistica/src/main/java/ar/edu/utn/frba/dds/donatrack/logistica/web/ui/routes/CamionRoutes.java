package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers.CamionController;
import io.javalin.config.RoutesConfig;

public class CamionRoutes {

  public CamionRoutes() { }

  public static void registrar(RoutesConfig ruta, CamionController controller) {
    ruta.get("/camiones", controller::mostrarTodos);
    ruta.get("/camiones/nuevo", controller::mostrarFormularioCreacion);
    ruta.post("/camiones", controller::guardar);
    ruta.get("/camiones/{patente}/edicion", controller::mostrarFormularioEdicion);
    ruta.post("/camiones/{patente}/edicion", controller::actualizar);
    ruta.post("/camiones/{patente}/eliminacion", controller::eliminar);
  }

}
