package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

  public void mostrarHome(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    Map<String, Object> modelo = new HashMap<>();

    if(usuarioActual != null) {
      modelo.put("usuario", usuarioActual);
      modelo.put("saludo", "¡Bienvenido al sistema DonaTrack! " + usuarioActual.getNombreUsuario() + " un gusto volver a verlo");
      context.render("views/home.hbs", modelo);
    }
    else
      context.render("views/general/landing.hbs");
  }

}