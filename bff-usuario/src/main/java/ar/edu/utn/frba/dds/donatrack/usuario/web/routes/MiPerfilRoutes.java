package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MiPerfilController;
import io.javalin.config.RoutesConfig;

public class MiPerfilRoutes {
  private MiPerfilRoutes() { }

  public static void registrar(RoutesConfig ruta, MiPerfilController controller) {
    ruta.get("/perfil", controller::mostrarPerfil);
    ruta.get("/perfil/contrasena", controller::mostrarFormularioContrasena);
    ruta.post("/perfil/contrasena", controller::cambiarContrasena);
  }
}