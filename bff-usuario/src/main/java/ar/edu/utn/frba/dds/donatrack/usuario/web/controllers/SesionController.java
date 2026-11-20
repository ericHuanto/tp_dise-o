package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.NotificacionPinRequest;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.generadorhash.Integridad;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.recuperacionusuario.CacheRecuperacion;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SesionController implements WithSimplePersistenceUnit {
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;
  private final UsuarioRepository repoUsuarios;

  public SesionController(DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub, UsuarioRepository usuarioRepository) {
    this.donacionesStub = donacionesStub;
    this.repoUsuarios = usuarioRepository;
  }

  public void mostrarFormularioInicioSesion(Context context) {
    String errorFlash = context.consumeSessionAttribute("errorFlash");
    String exitoFlash = context.consumeSessionAttribute("exitoFlash");
    String nombreUsuario = context.consumeSessionAttribute("nombreEscrito");

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("mensajeError", errorFlash);
    modelo.put("mensajeExito", exitoFlash);
    modelo.put("nombreEscritoAnteriormente", nombreUsuario);

    context.render("comun/views/auth/login.hbs", modelo);
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
      context.sessionAttribute("usuario_actual", usuario);
      context.redirect("/");
    }
  }

  public void solicitarPin(Context context) {
    String nombreUsuario = context.formParam("name");

    if (nombreUsuario == null || nombreUsuario.isBlank()) {
      context.sessionAttribute("errorFlash", "Escribe tu usuario para recuperar la contraseña.");
      context.redirect("/login");
      return;
    }

    Usuario usuario = repoUsuarios.buscarPorCredenciales(nombreUsuario);

    if (usuario != null) {
      String pin = UUID.randomUUID().toString().substring(0, 8);
      CacheRecuperacion.guardarPin(usuario.getNombreUsuario(), pin);
      //List<MedioContacto> contactos = repoUsuarios.buscarMedioContacto(usuario.getIdBeneficiario(), usuario.getIdDonante()); //TODO el medio de contacto no es de este repo. como lo hago ?
      //contactos.forEach(c -> c.recibirMensaje("su código de recuperación de contacto es: "+ pin));
      NotificacionPinRequest request = NotificacionPinRequest.newBuilder()
          .setIdBeneficiario(usuario.getIdBeneficiario() != null ? usuario.getIdBeneficiario() : 0)
          .setIdDonante(usuario.getIdDonante() != null ? usuario.getIdDonante() : 0)
          .setPin(pin)
          .build();

      donacionesStub.enviarPinRecuperacion(request);
    }

    context.sessionAttribute("infoFlash", nombreUsuario + ", te enviamos un código a tu correo. Tienes 15 minutos para usarlo.");
    context.redirect("/recuperacion?usuario=" + nombreUsuario);
  }

  public void mostrarFormularioRecuperacion(Context context) {
    String infoFlash = context.consumeSessionAttribute("infoFlash");
    String errorFlash = context.consumeSessionAttribute("errorFlash");
    String nombreUsuario = context.queryParam("usuario");

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("informacion", infoFlash);
    modelo.put("errorFlash", errorFlash);
    modelo.put("nombreUsuario", nombreUsuario);

    context.render("comun/views/auth/recuperar_cuenta.hbs", modelo);
  }

  public void actualizarContraseña(Context context) {
    String pinIngresado = context.formParam("pin");
    String nuevaContraseña = context.formParam("new_password");
    String nombreUsuario = context.formParam("nombre_usuario");

    if (nombreUsuario == null || nombreUsuario.isEmpty()) {
      context.sessionAttribute("errorFlash", "Nombre de usuario vacío. Vuelve a intentarlo");
      context.redirect("/login");
      return;
    }

    String pinGuardado = CacheRecuperacion.obtenerPin(nombreUsuario);

    if (pinGuardado == null) {
      context.sessionAttribute("errorFlash", "El código expiró o no existe. Solicita uno nuevo.");
      context.redirect("/login");
      return;
    }

    if (!pinGuardado.equals(pinIngresado)) {
      context.sessionAttribute("errorFlash", "El código es incorrecto.");
      context.redirect("/recuperacion?usuario=" + nombreUsuario);
      return;
    }

    Usuario usuario = repoUsuarios.buscarPorCredenciales(nombreUsuario);
    usuario.actualizarContraseña(nuevaContraseña);
    try {
      beginTransaction();
      repoUsuarios.actualizar(usuario);
      commitTransaction();
    } catch (Exception e) {
      context.sessionAttribute("errorFlash", "Ocurrió un error inesperado. Al actualizar la contraseña vuelva a intentarlo");
      context.redirect("/login");
      return;
    }

    CacheRecuperacion.borrarPin(nombreUsuario);
    context.sessionAttribute("exitoFlash", "Contraseña actualizada. Ya puedes iniciar sesión.");
    context.redirect("/login");
  }

  public void cerrarSesion(Context context) {
    context.req().getSession().invalidate();
    context.redirect("/");
  }
}