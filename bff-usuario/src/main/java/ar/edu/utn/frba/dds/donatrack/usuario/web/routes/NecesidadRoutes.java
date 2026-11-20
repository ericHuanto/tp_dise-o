package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.NecesidadController;
import io.javalin.config.RoutesConfig;

public class NecesidadRoutes {

  private NecesidadRoutes() { }

  public static void registrar(RoutesConfig ruta, NecesidadController controller) {
    ruta.get("/mis-necesidades", controller::listar, Permiso.SOLICITAR_AYUDA);
    ruta.get("/mis-necesidades/nueva", controller::mostrarFormulario, Permiso.SOLICITAR_AYUDA);
    ruta.post("/mis-necesidades", controller::guardar, Permiso.SOLICITAR_AYUDA);
    ruta.get("/mis-necesidades/{id}/editar", controller::mostrarFormularioEdicion, Permiso.SOLICITAR_AYUDA);
    ruta.post("/mis-necesidades/{id}/editar", controller::actualizar, Permiso.SOLICITAR_AYUDA);
  }
}