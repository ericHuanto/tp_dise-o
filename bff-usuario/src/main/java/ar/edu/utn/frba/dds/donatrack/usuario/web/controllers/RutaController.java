package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.ListaMarcadoresCamionResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc.LogisticaServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.MarcadorCamionMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.VacioLogistica;
import com.google.gson.Gson;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RutaController {
  private final LogisticaServiceBlockingStub logisticaStub;

  public RutaController(LogisticaServiceBlockingStub logisticaStub) {
    this.logisticaStub = logisticaStub;
  }

  public void verSeguimiento(Context context){
    ListaMarcadoresCamionResponse response = logisticaStub.obtenerSeguimientoEntregas(
        VacioLogistica.getDefaultInstance()
    );

    List<Map<String, Object>> marcadores = new ArrayList<>();
    for(MarcadorCamionMsg m : response.getMarcadoresList()) {
      Map<String, Object> marcador = new HashMap<>();
      marcador.put("patente", m.getPatente());
      marcador.put("latitud", m.getLatitud());
      marcador.put("longitud", m.getLongitud());
      marcador.put("ultimaActualizacion", m.getUltimaActualizacion());
      marcadores.add(marcador);
    }

    String marcadoresJson = new Gson().toJson(marcadores);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("marcadoresJson", marcadoresJson);

    context.render("logistica/views/seguimiento_entregas.hbs", modelo);
  }
}
