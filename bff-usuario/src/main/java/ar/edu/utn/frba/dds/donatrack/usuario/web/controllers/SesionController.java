package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.generadorhash.Integridad;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class SesionController implements WithSimplePersistenceUnit {
  private final UsuarioRepository repoUsuarios;

  public SesionController(UsuarioRepository usuarioRepository) {
    this.repoUsuarios = usuarioRepository;
  }

  public void mostrarFormularioInicioSesion(Context context) {
    String errorFlash = context.consumeSessionAttribute("errorFlash");
    String nombreUsuario = context.consumeSessionAttribute("nombreEscrito");

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("mensajeError", errorFlash);
    modelo.put("nombreEscritoAnteriormente", nombreUsuario);

    context.render("donaciones/views/auth/login.hbs", modelo);
  }

  public void iniciarSesion(Context context) {
    String nombreUsuario = context.formParam("name");
    String contraseña = context.formParam("password");

    Usuario usuario = repoUsuarios.buscarPorCredenciales(nombreUsuario);

    if (usuario == null) {
      context.sessionAttribute("errorFlash", "El usuario no existe");
      context.sessionAttribute("nombreEscrito", nombreUsuario);
      context.redirect("/login");
    } else if (!Integridad.validar(contraseña, usuario.getContraseña())) {
      context.sessionAttribute("errorFlash", "La contraseña es incorrecta");
      context.sessionAttribute("nombreEscrito", nombreUsuario);
      context.redirect("/login");
    } else {
      usuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", usuario);
      context.redirect("/");
    }
  }

  public void cerrarSesion(Context context) {
    context.req().getSession().invalidate();
    context.redirect("/");
  }
}