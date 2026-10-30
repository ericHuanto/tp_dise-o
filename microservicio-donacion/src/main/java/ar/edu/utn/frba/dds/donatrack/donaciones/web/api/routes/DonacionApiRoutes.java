package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.DonacionApiController;
import io.javalin.config.RoutesConfig;

public class DonacionApiRoutes {

  private DonacionApiRoutes() {}

  public static void registrar(RoutesConfig ruta, DonacionApiController controller) {
    ruta.post("/api/donaciones", controller::crear);//paso 1 En Depósito -- el paso 2 está en el proceso matchmaking
    ruta.get("/api/donaciones", controller::obtenerTodos);
    ruta.get("/api/donaciones/{id}", controller::obtener);
    ruta.patch("/api/donaciones/{id}", controller::actualizar);
    ruta.delete("/api/donaciones/{id}", controller::eliminar);
    ruta.get("/api/donaciones/{id}/estados", controller::historialEstados);
    ruta.patch("/api/donaciones/{id}/lista-para-entregar", controller::donacionListaParaEntregar); //paso 3 Lista Para Entregar
    ruta.patch("/api/donaciones/{id}/en-camino", controller::donacionEnCamino); //paso 4 En Traslado
    ruta.patch("/api/donaciones/{id}/entregada", controller::donacionEntregada); //paso 5 Entregada (Fin)
    ruta.patch("/api/donaciones/{id}/error-entrega", controller::donacionEntregaFallida); //paso 5B
    ruta.patch("/api/donaciones/{id}/vuelta-deposito", controller::donacionDevueltaADeposito); //paso 6B que viene de 5B
    ruta.patch("/api/donaciones/{id}/vencida", controller::donacionVencida); //paso 2B (Fin)
  }

}
