package ar.edu.utn.frba.dds.donatrack.shared;

import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BaseDatoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RegistroNoEncontradoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BodyException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.CambioDeEstadoNoPermitidoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;

import java.io.FileNotFoundException;

public class ExceptionHandlers {

  public record ErrorResponse(int status, String error) { }

  private ExceptionHandlers() {}

  public static void registrar(RoutesConfig ruta) {
    // JSON Mal formado
    ruta.exception(JsonSyntaxException.class, (e, ctx) ->
      ctx.status(400).json(new ErrorResponse(400, "El body no es un JSON válido")));
    // Error en el Body
    ruta.exception(BodyException.class, (e, ctx) ->
      ctx.status(400).json(new ErrorResponse(400, e.getMessage())));
    // Validación de Dominio
    ruta.exception(DominioException.class, (e, ctx) ->
        ctx.status(400).json(new ErrorResponse(400, e.getMessage())));
    // Recurso no encontrado
    ruta.exception(RecursoNoEncontradoException.class, (e, ctx) ->
        ctx.status(404).json(new ErrorResponse(404, e.getMessage())));
    // Registro no encontrado en BD
    ruta.exception(RegistroNoEncontradoException.class, (e, ctx) ->
        ctx.status(404).json(new ErrorResponse(404, e.getMessage())));
    // Conflicto de estado
    ruta.exception(CambioDeEstadoNoPermitidoException.class, (e, ctx) ->
        ctx.status(409).json(new ErrorResponse(409, e.getMessage())));
    // Error en la BD
    ruta.exception(BaseDatoException.class, (e, ctx) -> {
      ctx.status(500).json(new ErrorResponse(500, e.getMessage()));
    });
    // Archivo .hbs No encontrado
    ruta.exception(FileNotFoundException.class, (e, ctx) -> {
      System.err.println("[Vista] Error de renderizado: ");
      System.out.print("Faltó crear el archivo -> " + e.getMessage());
      ctx.status(500).html("<h2>Error Interno: Vista no encontrada</h2>" + "<p>El sistema intentó cargar una pantalla, que no existe.</p>");
    });
    // Comodín atrapa todas las excepciones que no fueron, registradas
    ruta.exception(Exception.class, (e, ctx) -> {
      ctx.status(500).json(new ErrorResponse(500, "Error interno del servidor"));
      e.printStackTrace();
    });
  }

}