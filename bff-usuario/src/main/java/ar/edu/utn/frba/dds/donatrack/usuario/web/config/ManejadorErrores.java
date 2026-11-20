package ar.edu.utn.frba.dds.donatrack.usuario.web.config;

import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import io.grpc.StatusRuntimeException;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejo de errores del BFF: ante cualquier problema, el usuario ve la pantalla de error
 * del sistema (comun/views/error.hbs) en lugar de un stack trace, un JSON o un texto plano.
 *
 * Cubre dos casos:
 *  1. Excepciones que se escapan de un controller  -> ruta.exception(...)
 *  2. Respuestas con código de error sin excepción  -> ruta.error(...)
 *     (una URL que no existe, o un controller que hizo context.status(404).result("..."))
 *
 * Las rutas /api/* (las consume JavaScript) siguen respondiendo JSON.
 */
public final class ManejadorErrores {
  private static final String YA_MOSTRADO = "error_ya_mostrado";
  private static final List<Integer> CODIGOS = List.of(400, 401, 403, 404, 405, 409, 500, 503);

  private ManejadorErrores() {}

  public static void registrar(RoutesConfig ruta) {

    // Errores que devuelve un microservicio por gRPC
    ruta.exception(StatusRuntimeException.class, (e, ctx) -> {
      int codigo = codigoHttp(e);
      if (codigo >= 500) e.printStackTrace();
      mostrar(ctx, codigo, codigo < 500 ? e.getStatus().getDescription() : null);
    });

    // Errores propios del dominio (validaciones)
    ruta.exception(DominioException.class, (e, ctx) -> mostrar(ctx, 400, e.getMessage()));
    ruta.exception(RecursoNoEncontradoException.class, (e, ctx) -> mostrar(ctx, 404, e.getMessage()));

    // Respuestas de Javalin lanzadas como excepción (NotFoundResponse, ForbiddenResponse, RedirectResponse...)
    ruta.exception(HttpResponseException.class, (e, ctx) -> {
      if (e.getStatus() < 400) {          // una redirección no es un error: se deja pasar
        ctx.status(e.getStatus());
        return;
      }
      mostrar(ctx, e.getStatus(), null);
    });

    ruta.exception(Exception.class, (e, ctx) -> {
      e.printStackTrace();
      mostrar(ctx, 500, null);
    });

    // Códigos de error sin excepción
    for (int codigo : CODIGOS) {
      ruta.error(codigo, ctx -> {
        if (Boolean.TRUE.equals(ctx.attribute(YA_MOSTRADO))) return;

        //Si el controller ya respondió con una página o con JSON, no se pisa:
        String tipo = ctx.contentType();
        if (tipo != null && !tipo.contains("text/plain")) return;

        mostrar(ctx, codigo, ctx.result());   // el texto del controller, si lo hay, se muestra como detalle
      });
    }
  }

  //============= FUNCIONES AUXILIARES =====================

  private static void mostrar(Context ctx, int codigo, String detalle) {
    ctx.attribute(YA_MOSTRADO, true);
    ctx.status(codigo);
    String detalleLimpio = (detalle == null || detalle.isBlank() || detalle.trim().startsWith("<")) ? null : detalle.trim();

    // Rutas que consume JavaScript con fetch: siguen siendo JSON
    if (ctx.path().startsWith("/api/")) {
      ctx.json(Map.of("mensaje", detalleLimpio != null ? detalleLimpio : titulo(codigo)));
      return;
    }

    // Archivos estáticos inexistentes (.css, .js, imágenes): solo el código, sin página
    if (ctx.path().matches(".*\\.[a-zA-Z0-9]{2,5}$")) {
      ctx.result("");
      return;
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", ctx.sessionAttribute("usuario_actual"));   // para que el header muestre el navbar
    modelo.put("codigo", codigo);
    modelo.put("titulo", titulo(codigo));
    modelo.put("mensaje", mensaje(codigo));
    modelo.put("detalle", detalleLimpio);

    try {
      ctx.render("comun/views/error.hbs", modelo);
    } catch (Exception e) {
      // Si hasta la pantalla de error falla, una versión mínima en HTML puro
      e.printStackTrace();
      ctx.html("<h1>Error " + codigo + "</h1><p>" + mensaje(codigo) + "</p><p><a href=\"/\">Volver al inicio</a></p>");
    }
  }

  private static int codigoHttp(StatusRuntimeException e) {
    return switch (e.getStatus().getCode()) {
      case INVALID_ARGUMENT -> 400;
      case UNAUTHENTICATED -> 401;
      case PERMISSION_DENIED -> 403;
      case NOT_FOUND -> 404;
      case ALREADY_EXISTS, FAILED_PRECONDITION -> 409;
      case UNAVAILABLE, DEADLINE_EXCEEDED -> 503;
      default -> 500;
    };
  }

  private static String titulo(int codigo) {
    return switch (codigo) {
      case 400 -> "Hay un problema con los datos";
      case 401 -> "Tenés que iniciar sesión";
      case 403 -> "No tenés acceso a esta página";
      case 404 -> "No encontramos lo que buscás";
      case 405 -> "Acción no permitida";
      case 409 -> "No se pudo completar la acción";
      case 503 -> "Servicio no disponible";
      default -> "Ocurrió un problema inesperado";
    };
  }

  private static String mensaje(int codigo) {
    return switch (codigo) {
      case 400 -> "Revisá la información que enviaste y volvé a intentarlo.";
      case 401 -> "Iniciá sesión para continuar.";
      case 403 -> "Tu usuario no tiene permisos para ver este contenido.";
      case 404 -> "La página o el recurso que buscás no existe o fue movido.";
      case 405 -> "Esta acción no está disponible desde acá.";
      case 409 -> "El estado actual no permite realizar esta acción.";
      case 503 -> "Una parte del sistema no está respondiendo. Probá de nuevo en unos minutos.";
      default -> "No pudimos procesar tu solicitud en este momento. Por favor, volvé a intentarlo.";
    };
  }
}