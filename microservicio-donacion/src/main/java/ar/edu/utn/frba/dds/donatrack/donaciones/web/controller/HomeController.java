package ar.edu.utn.frba.dds.donatrack.donaciones.web.controller;

import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

  public void index(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("saludo", "¡Bienvenido al sistema DonaTrack!, un gusto volver a verlo");

    boolean estaAutenticado = false;
    if(estaAutenticado)
      context.render("home.hbs", modelo);
    else
      context.render("landing_page.hbs");
  }

}