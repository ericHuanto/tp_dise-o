package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CamionController;
import io.javalin.config.RoutesConfig;

public class CamionRoutes {

  public CamionRoutes() { }

  public static void registrar(RoutesConfig ruta, CamionController controller) {
    ruta.get("/camiones", controller::mostrarTodos, Permiso.GESTIONAR_CAMIONES);
    ruta.get("/camiones/nuevo", controller::mostrarFormularioCreacion, Permiso.GESTIONAR_CAMIONES);
    ruta.post("/camiones", controller::crear, Permiso.GESTIONAR_CAMIONES);
    ruta.get("/camiones/{patente}/edicion", controller::mostrarFormularioEdicion, Permiso.GESTIONAR_CAMIONES);
    ruta.post("/camiones/{patente}/edicion", controller::actualizar, Permiso.GESTIONAR_CAMIONES);
    ruta.post("/camiones/{patente}/eliminacion", controller::eliminar, Permiso.GESTIONAR_CAMIONES);
  }
}