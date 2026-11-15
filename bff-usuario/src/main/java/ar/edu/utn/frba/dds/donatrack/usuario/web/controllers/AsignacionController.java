package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.ConfirmarAsignacionRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaRankingsResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionController {
  private final DonacionesServiceBlockingStub donacionesStub;

  public AsignacionController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarTodos(Context context) {
    ListaRankingsResponse response = donacionesStub.listarRankingsVigentes(Vacio.getDefaultInstance());

    List<Map<String, Object>> rankingsVista = response.getRankingsList().stream().map(r -> {
      Map<String, Object> donacionMap = Map.of(
          "id", r.getDonacion().getId(),
          "descripcion", r.getDonacion().getDescripcion(),
          "estado", r.getDonacion().getEstado(),
          "donantes", r.getDonacion().getDonantesList()
      );
      return Map.<String, Object>of(
          "id", r.getId(),
          "fechaGeneracion", r.getFechaGeneracion(),
          "donacion", donacionMap,
          "candidatos", r.getCandidatosList()
      );
    }).toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("rankings", rankingsVista);

    context.render("views/admin/asignar_beneficiaria.hbs", modelo);
  }

  public void confirmarAsignacion(Context context) {
    Long idRanking = Long.parseLong(context.pathParam("id"));
    Long idBeneficiario = Long.parseLong(context.formParam("id_beneficiario"));

    donacionesStub.confirmarAsignacion(
        ConfirmarAsignacionRequest.newBuilder()
            .setIdRanking(idRanking)
            .setIdBeneficiario(idBeneficiario)
            .build()
    );

    context.redirect("/rankings");
  }
}