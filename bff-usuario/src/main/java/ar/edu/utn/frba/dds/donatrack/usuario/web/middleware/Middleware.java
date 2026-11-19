package ar.edu.utn.frba.dds.donatrack.usuario.web.middleware;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.security.RouteRole;

import java.util.List;
import java.util.Set;

public class Middleware {

  public static void verificarPermisos(Context ctx) {
    Set<RouteRole> permisosRuta = ctx.routeRoles();

    if (permisosRuta.isEmpty()) {
      return;
    }

    Usuario usuario = ctx.sessionAttribute("usuario_actual");
    if (usuario == null) {
      ctx.redirect("/");
      ctx.skipRemainingHandlers();
      return;
    }

    boolean autorizado = permisosRuta.stream().anyMatch(rol -> usuario.tienePermiso((Permiso) rol));

    if (!autorizado) {
      System.err.println("No tienes permiso");
      throw new ForbiddenResponse("No tenés permisos para acceder a esta sección.");
    }
  }

}
