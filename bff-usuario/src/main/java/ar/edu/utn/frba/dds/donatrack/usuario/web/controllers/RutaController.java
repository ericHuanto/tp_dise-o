package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.CamionDonacionesMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionVistaMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesIdsRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.DonanteIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.FiltroDonacionesDonanteRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaCamionesDonacionesResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaMarcadoresCamionResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc.LogisticaServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.MarcadorCamionMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.grpc.VacioLogistica;
import ar.edu.utn.frba.dds.donatrack.grpc.VistaDonacionesDonanteResponse;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.CamionSeguimiento;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RutaController {
  private final LogisticaServiceBlockingStub logisticaStub;
  private final DonacionesServiceBlockingStub donacionesStub;

  public RutaController(LogisticaServiceBlockingStub logisticaStub, DonacionesServiceBlockingStub donacionesStub) {
    this.logisticaStub = logisticaStub;
    this.donacionesStub = donacionesStub;
  }

  public void verSeguimiento(Context context) {
    Usuario usuario = context.sessionAttribute("usuario_actual");

    if (usuario == null) {
      context.redirect("/login");
      return;
    }

    boolean esDonante = usuario.getIdDonante() != null;
    boolean esBeneficiario = usuario.getIdBeneficiario() != null;

    record DonacionComun(Long id, String estadoActual, String descripcion) {}
    List<DonacionComun> todasLasDonaciones = new ArrayList<>();

    if (esDonante) {
      donacionesStub.obtenerVistaDonacionesDonante(
          FiltroDonacionesDonanteRequest.newBuilder().setIdDonante(usuario.getIdDonante()).build()
      ).getDonacionesList().forEach(d ->
          todasLasDonaciones.add(new DonacionComun(d.getId(), d.getEstadoActual(), d.getDescripcion()))
      );
    } else if (esBeneficiario) {
      donacionesStub.listarDonacionesPorBeneficiario(
          BeneficiarioIdRequest.newBuilder().setId(usuario.getIdBeneficiario()).build()
      ).getDonacionesList().forEach(d ->
          todasLasDonaciones.add(new DonacionComun(
              d.getId(),
              d.getEstadoActual(),
              d.getDescripcion()
          ))
      );
    }

    List<DonacionComun> donacionesEnTraslado = todasLasDonaciones.stream()
        .filter(d -> "EN_TRASLADO".equals(d.estadoActual()))
        .toList();

    List<Long> idsEnTraslado = donacionesEnTraslado.stream()
        .map(DonacionComun::id)
        .toList();

    List<CamionSeguimiento> camionesVista = new ArrayList<>();
    String camionesJson = "[]";

    if (!idsEnTraslado.isEmpty()) {
      var coordsPorDestino = obtenerCoordenadasBeneficiarios();

      ListaCamionesDonacionesResponse logisticaResponse = logisticaStub.obtenerCamionesDeDonaciones(
          DonacionesIdsRequest.newBuilder().addAllIdsDonaciones(idsEnTraslado).build()
      );

      for (CamionDonacionesMsg cam : logisticaResponse.getCamionesList()) {
        String[] partes = cam.getUltimaActualizacion().split("\\|", -1);
        String tiempoAct = partes.length > 0 ? partes[0] : "hace 3 min";
        String destinoEntrega = partes.length > 1 ? partes[1] : "Sin destino asignado";
        String ubicacionEntrega = partes.length > 2 ? partes[2] : "Dirección no disponible";

        String[] coordsDest = coordsPorDestino.getOrDefault(
            destinoEntrega.trim().toLowerCase(),
            new String[]{"", ""}
        );

        List<CamionSeguimiento.DetalleDonacion> detallesDonaciones = new ArrayList<>();
        for (Long idDon : cam.getIdsDonacionesList()) {
          String desc = todasLasDonaciones.stream()
              .filter(d -> d.id().equals(idDon))
              .map(DonacionComun::descripcion)
              .findFirst()
              .orElse("Donación #" + idDon);

          detallesDonaciones.add(CamionSeguimiento.DetalleDonacion.builder()
              .codigo("#" + idDon)
              .descripcion(desc)
              .destino(destinoEntrega)
              .ubicacion(ubicacionEntrega)
              .latitudDestino(coordsDest[0])
              .longitudDestino(coordsDest[1])
              .build());
        }

        camionesVista.add(new CamionSeguimiento(
            cam.getPatente(),
            cam.getLatitud(),
            cam.getLongitud(),
            tiempoAct,
            detallesDonaciones
        ));
      }
      camionesJson = new Gson().toJson(camionesVista);
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuario);
    modelo.put("esDonante", esDonante);
    modelo.put("camiones", camionesVista);
    modelo.put("camionesJson", camionesJson);
    modelo.put("logisticaDisponible", true);

    context.render("donaciones/views/general/seguimiento_entregas.hbs", modelo);
  }

  private Map<String, String[]> obtenerCoordenadasBeneficiarios() {
    Map<String, String[]> mapa = new HashMap<>();
    try {
      String json = donacionesStub.obtenerDatosMapaDonacionesRecibidas(Vacio.getDefaultInstance())
          .getMarcadoresJson();
      JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
      for (JsonElement el : arr) {
        JsonObject obj = el.getAsJsonObject();
        String nombre = obj.has("nombre") ? obj.get("nombre").getAsString()
            : (obj.has("razonSocial") ? obj.get("razonSocial").getAsString() : "");
        String lat = obj.has("latitud") ? obj.get("latitud").getAsString()
            : (obj.has("lat") ? obj.get("lat").getAsString() : "");
        String lon = obj.has("longitud") ? obj.get("longitud").getAsString()
            : (obj.has("lng") ? obj.get("lng").getAsString() : "");
        if (!nombre.isBlank() && !lat.isBlank() && !lon.isBlank()) {
          mapa.put(nombre.trim().toLowerCase(), new String[]{lat, lon});
        }
      }
    } catch (Exception ignored) {
    }
    return mapa;
  }
}