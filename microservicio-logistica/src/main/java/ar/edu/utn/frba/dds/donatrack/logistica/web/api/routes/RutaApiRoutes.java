package ar.edu.utn.frba.dds.donatrack.logistica.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers.RutaApiController;
import io.javalin.config.RoutesConfig;

public class RutaApiRoutes {

  private RutaApiRoutes() {}

  public static void registrar(RoutesConfig ruta, RutaApiController controller) {
    ruta.get("/api/rutas", controller::obtenerTodas);
    ruta.get("/api/rutas/{id}", controller::obtener);
    ruta.patch("/api/rutas/{id}/chofer", controller::asignarChofer);
    ruta.patch("/api/rutas/{id}/inicio", controller::iniciar); //activa el paso 4 de la donación (En Traslado)
    //Todo descpues cambiarlo a otra carpeta
    ruta.get("/seguimiento_entregas", controller::verSeguimiento);
  }

}