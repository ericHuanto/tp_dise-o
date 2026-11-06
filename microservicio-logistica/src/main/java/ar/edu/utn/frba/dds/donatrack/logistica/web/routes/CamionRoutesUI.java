package ar.edu.utn.frba.dds.donatrack.logistica.web.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.CamionControllerUI;
import io.javalin.config.RoutesConfig;

public class CamionRoutesUI {

  public CamionRoutesUI() { }

  public static void registrar(RoutesConfig ruta, CamionControllerUI controller) {
    ruta.get("/camiones", controller::mostrarTodos);
    ruta.get("/camiones/nuevo", controller::mostrarFormularioCreacion);
    ruta.post("/camiones", controller::guardar);
    ruta.get("/camiones/{patente}/edicion", controller::mostrarFormularioEdicion);
    ruta.post("/camiones/{patente}/edicion", controller::actualizar);
    ruta.post("/camiones/{patente}/eliminacion", controller::eliminar);
  }

}
