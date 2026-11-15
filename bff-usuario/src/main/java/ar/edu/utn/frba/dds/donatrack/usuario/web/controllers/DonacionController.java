package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.FiltroEstadoDonacionRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonacionesAdminResponse;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class DonacionController {
  private final DonacionesServiceBlockingStub donacionesStub;

  public DonacionController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarDonaciones(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    String estadoQuery = context.queryParam("estado_actual");

    FiltroEstadoDonacionRequest request = FiltroEstadoDonacionRequest.newBuilder()
        .setEstadoActual(estadoQuery != null ? estadoQuery : "")
        .build();

    ListaDonacionesAdminResponse response = donacionesStub.listarDonacionesAdmin(request);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("donaciones", response.getDonacionesList());

    context.render("views/admin/donaciones_en_deposito.hbs", modelo);
  }

  public void marcarComoVencida(Context context) {
    Long idDonacion = Long.parseLong(context.pathParam("id"));

    donacionesStub.marcarDonacionComoVencida(
        DonacionId.newBuilder().setId(idDonacion).build()
    );

    context.redirect("/donaciones?estado_actual=en_deposito");
  }
}