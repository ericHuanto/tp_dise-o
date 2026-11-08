package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Coordenada;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import com.google.gson.Gson;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RutaController {
  private RutaRepository repoRutas;

  public RutaController(RutaRepository rutaRepository) {
    this.repoRutas = rutaRepository;
  }

  public void verSeguimiento(Context ctx) throws Exception {
    List<Map<String, Object>> marcadores = new ArrayList<>();

    for (Ruta ruta : repoRutas.buscarTodas()) {

      if (!ruta.isEstaIniciada()) {
        continue;
      }

      boolean tieneEntregasActivas = ruta.getEntregasOrdenadas()
          .stream()
          .anyMatch(entrega ->
              entrega.getEstadoActual() == TipoEstadoEntrega.EN_TRASLADO
          );

      if (!tieneEntregasActivas) {
        continue;
      }

      Camion camion = ruta.getCamion();
      Coordenada ubicacion = camion.getUbicacionActual();

      if (ubicacion == null) {
        continue;
      }

      Map<String, Object> marcador = new HashMap<>();

      marcador.put("patente", camion.getPatente());
      marcador.put("latitud", ubicacion.getLatitud());
      marcador.put("longitud", ubicacion.getLongitud());
      marcador.put("ultimaActualizacion", "No disponible");

      marcadores.add(marcador);
    }

    String marcadoresJson = new Gson().toJson(marcadores);
    ctx.render(
        "seguimiento_entregas.hbs",
        Map.of("marcadoresJson", marcadoresJson)
    );
  }

}
