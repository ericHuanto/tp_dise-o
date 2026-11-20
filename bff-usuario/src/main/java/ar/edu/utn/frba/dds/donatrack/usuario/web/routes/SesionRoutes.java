package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.SesionController;
import io.javalin.config.RoutesConfig;

public class SesionRoutes {

  private SesionRoutes() { }

  public static void registrar(RoutesConfig ruta, SesionController controller) {
    ruta.get("/login", controller::mostrarFormularioInicioSesion);
    ruta.post("/login", controller::iniciarSesion);
    ruta.post("/solicitacion-pin", controller::solicitarPin);
    ruta.get("/recuperacion", controller::mostrarFormularioRecuperacion);
    ruta.post("/recuperacion", controller::actualizarContraseña);
    ruta.post("/logout", controller::cerrarSesion);
  }
}