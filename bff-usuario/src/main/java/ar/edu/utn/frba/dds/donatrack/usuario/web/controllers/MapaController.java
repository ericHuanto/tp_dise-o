package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DatosMapaResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.CategoriaVistaDto;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaController {
  private final DonacionesServiceBlockingStub donacionesStub;

  public MapaController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarDonacionesRecibidas(Context context) {
    DatosMapaResponse response = donacionesStub.obtenerDatosMapaDonacionesRecibidas(
        Vacio.getDefaultInstance()
    );

    List<CategoriaVistaDto> categorias = response.getCategoriasList().stream()
        .map(c -> new CategoriaVistaDto(c.getId(), c.getNombre()))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("marcadoresJson", response.getMarcadoresJson());
    modelo.put("categorias", categorias);

    context.render("donaciones/views/general/mapa_beneficiarios.hbs", modelo);
  }
}