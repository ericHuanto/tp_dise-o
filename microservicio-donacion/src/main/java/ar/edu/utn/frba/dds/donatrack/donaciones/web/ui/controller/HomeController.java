package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

  public void mostrarHome(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if(usuarioActual != null) {
      modelo.put("saludo", "¡Bienvenido al sistema DonaTrack! " + usuarioActual.getNombreUsuario() + " un gusto volver a verlo");
      context.render("home.hbs", modelo);
    }
    else
      context.render("landing_page.hbs");
  }

}