package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DetalleDonacionBeneficiarioRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DetalleDonacionBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonacionesBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class DonacionBeneficiarioController {

  private final DonacionesServiceBlockingStub donacionesStub;

  public DonacionBeneficiarioController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void listar(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario()) {
      context.status(403).result("No tenés permiso para consultar donaciones");
      return;
    }

    if (usuarioActual.getIdBeneficiario() == null) {
      context.status(404).result("No existe un beneficiario asociado a este usuario");
      return;
    }

    try {
      ListaDonacionesBeneficiarioResponse response = donacionesStub.listarDonacionesPorBeneficiario(
          BeneficiarioIdRequest.newBuilder()
              .setId(usuarioActual.getIdBeneficiario())
              .build()
      );

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("donaciones", response.getDonacionesList());

      context.render("donaciones/views/donante/mis_donaciones.hbs", modelo);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        context.status(404).result(e.getStatus().getDescription());
      } else {
        throw e;
      }
    }
  }

  public void verDetalle(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario()) {
      context.status(403).result("No tenés permiso para consultar donaciones");
      return;
    }

    if (usuarioActual.getIdBeneficiario() == null) {
      context.status(404).result("Beneficiario no encontrado");
      return;
    }

    Long idDonacion;
    try {
      idDonacion = Long.valueOf(context.pathParam("id"));
    } catch (NumberFormatException e) {
      context.status(400).result("ID de donación inválido");
      return;
    }

    try {
      DetalleDonacionBeneficiarioResponse response = donacionesStub.obtenerDetalleDonacionBeneficiario(
          DetalleDonacionBeneficiarioRequest.newBuilder()
              .setIdBeneficiario(usuarioActual.getIdBeneficiario())
              .setIdDonacion(idDonacion)
              .build()
      );

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("donacion", response.getDonacion());
      modelo.put("fechaAsignacion", response.getFechaAsignacion());

      context.render("donaciones/views/donante/detalle_donacion.hbs", modelo);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        context.status(404).result(e.getStatus().getDescription());
      } else {
        throw e;
      }
    }
  }
}