package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.utils.Integridad;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

public class SesionController implements WithSimplePersistenceUnit {
  private UsuarioRepository repoUsuarios;

  public SesionController(UsuarioRepository usuarioRepository) {
    this.repoUsuarios = usuarioRepository;
  }

  public void mostrarFormularioRegistro(Context context) {
    context.render("registro.hbs");
  }

  public void iniciarRegistro(Context context) {
    String nombreUsuario = context.formParam("name");
    String contraseñaPlana = context.formParam("password");

    Usuario usuarioExiste = repoUsuarios.buscarPorCredenciales(nombreUsuario);
    if (usuarioExiste != null) {
      context.redirect("/registro?error=El_nombre_ya_esta_en_uso");
      return;
    }
    Usuario usuarioNuevo = new Usuario(nombreUsuario, contraseñaPlana);

    try {
      beginTransaction();
      repoUsuarios.guardar(usuarioNuevo);
      commitTransaction();
      context.sessionAttribute("usuario_actual", usuarioNuevo);
      context.redirect("perfil/eleccion");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/registro?error=Error_interno");
    }
  }

  public void mostrarFormularioInicioSesion(Context context) {
    context.render("login.hbs");
  }

  public void iniciarSesion(Context context) {
    //Datos que recibo por el formulario
    String nombreUsuario = context.formParam("name");
    String contraseña = context.formParam("password");

    Usuario usuario = repoUsuarios.buscarPorCredenciales(nombreUsuario);

    if (usuario == null) {
      context.redirect("/login?error=El_usuario_no_existe");
    } else {
      if(!Integridad.validar(contraseña, usuario.getContraseña()))
        context.redirect("/login?error=La_contraseña_es_incorrecta");
      else {
        context.sessionAttribute("usuario_actual", usuario);
        context.redirect("/");
      }
    }
  }

  public void cerrarSesion(Context context) {
    context.req().getSession().invalidate();
    context.redirect("/");
  }

}
