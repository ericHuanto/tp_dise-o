package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.AsignacionController;
import io.javalin.config.RoutesConfig;

public class AsignacionRoutes {

  private AsignacionRoutes() { }

  public static void registrar(RoutesConfig ruta, AsignacionController controller) {
    ruta.get("/asignaciones", controller::mostrarTodos, Permiso.EDITAR_DONACION);
    ruta.get("/asignaciones/{id}", controller::mostrarDetalle, Permiso.EDITAR_DONACION);
    ruta.post("/asignaciones/{id}", controller::confirmarAsignacion, Permiso.EDITAR_DONACION);
  }
}