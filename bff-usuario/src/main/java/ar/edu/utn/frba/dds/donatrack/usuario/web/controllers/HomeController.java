package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

  public void mostrarLanding(Context context) {
    if (context.sessionAttribute("usuario_actual") != null) {
      context.redirect("/home");
      return;
    }
    context.render("comun/views/landing.hbs");
  }

  public void mostrarHome(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    Map<String, Object> modelo = new HashMap<>();

    if (usuarioActual != null) {
      modelo.put("usuario", usuarioActual);
      context.render("comun/views/home.hbs", modelo);
    } else {
      context.render("comun/views/landing.hbs");
    }
  }
}