package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers;

import io.javalin.http.Context;

public class HomeController {

  public void mostrarHome(Context ctx) {
    ctx.render("home.hbs");
  }

}
