package ar.edu.utn.frba.dds.donatrack.donaciones.web.routes;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.DonacionController;
import io.javalin.Javalin;
import io.javalin.config.RoutesConfig;

public class DonacionRoutes {

  private DonacionRoutes() {}

  public static void registrar(RoutesConfig ruta, DonacionController controller) {
    ruta.post("/donaciones", controller::crear);//paso 1 En Depósito -- el paso 2 está en el proceso matchmaking
    ruta.get("/donaciones", controller::obtenerTodos);
    ruta.get("/donaciones/{id}", controller::obtener);
    ruta.patch("/donaciones/{id}", controller::actualizar);
    ruta.delete("/donaciones/{id}", controller::eliminar);
    ruta.get("/donaciones/{id}/estados", controller::historialEstados);
    ruta.patch("/donaciones/{id}/lista-para-entregar", controller::donacionListaParaEntregar); //paso 3 Lista Para Entregar
    ruta.patch("/donaciones/{id}/en-camino", controller::donacionEnCamino); //paso 4 En Traslado
    ruta.patch("/donaciones/{id}/entregada", controller::donacionEntregada); //paso 5 Entregada (Fin)
    ruta.patch("/donaciones/{id}/error-entrega", controller::donacionEntregaFallida); //paso 5B
    ruta.patch("/donaciones/{id}/vuelta-deposito", controller::donacionDevueltaADeposito); //paso 6B que viene de 5B
    ruta.patch("/donaciones/{id}/vencida", controller::donacionVencida); //paso 2B (Fin)
  }

}
