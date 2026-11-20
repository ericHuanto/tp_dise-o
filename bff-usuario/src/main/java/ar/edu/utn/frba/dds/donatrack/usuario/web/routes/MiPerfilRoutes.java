package ar.edu.utn.frba.dds.donatrack.usuario.web.routes;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MiPerfilController;
import io.javalin.config.RoutesConfig;

public class MiPerfilRoutes {
  private MiPerfilRoutes() { }

  public static void registrar(RoutesConfig ruta, MiPerfilController controller) {
    ruta.get("/perfil", controller::mostrarPerfil, Permiso.AUTENTICADO);
    ruta.get("/perfil/contrasena", controller::mostrarFormularioContrasena, Permiso.AUTENTICADO);
    ruta.post("/perfil/contrasena", controller::cambiarContrasena, Permiso.AUTENTICADO);
  }
}