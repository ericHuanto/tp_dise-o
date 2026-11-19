package ar.edu.utn.frba.dds.donatrack.usuario.web.middleware;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import java.util.Set;

public class Middleware {

  public static void verificarPermisos(Context context) {
    Set<RouteRole> permisosRuta = context.routeRoles();

    if (permisosRuta.isEmpty()) return;

    Usuario usuario = context.sessionAttribute("usuario_actual");
    if (usuario == null) throw new UnauthorizedResponse("Necesitas iniciar sesión para acceder a esa vista.");

    boolean autorizado = permisosRuta.stream().anyMatch(rol -> usuario.tienePermiso((Permiso) rol));

    if (!autorizado) throw new ForbiddenResponse("Te faltan permisos para acceder a esa vista");
  }

}
