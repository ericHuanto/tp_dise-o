package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DetalleDonacionBeneficiarioRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DetalleDonacionBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionBeneficiarioMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonacionesBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaNovedadesBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.NovedadDonacionMsg;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.web.mappers.DonacionVistaMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionBeneficiarioController {

  private final DonacionesServiceBlockingStub donacionesStub;

  public DonacionBeneficiarioController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void listar(Context context) {
    Usuario usuarioActual = beneficiarioAutenticado(context, "No tenés permiso para consultar donaciones");
    if (usuarioActual == null) return;

    try {
      ListaDonacionesBeneficiarioResponse response = donacionesStub.listarDonacionesPorBeneficiario(
          BeneficiarioIdRequest.newBuilder()
              .setId(usuarioActual.getIdBeneficiario())
              .build()
      );

      List<Map<String, Object>> donacionesVista = response.getDonacionesList().stream()
          .map(this::aVistaDonacion)
          .toList();

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("donaciones", donacionesVista);

      context.render("donaciones/views/beneficiario/donaciones_asignadas.hbs", modelo);

    } catch (StatusRuntimeException e) {
      responderError(context, e);
    }
  }

  public void verDetalle(Context context) {
    Usuario usuarioActual = beneficiarioAutenticado(context, "No tenés permiso para consultar donaciones");
    if (usuarioActual == null) return;

    long idDonacion;
    try {
      idDonacion = Long.parseLong(context.pathParam("id"));
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
      modelo.put("donacion", aVistaDonacion(response.getDonacion()));
      modelo.put("fechaAsignacion", DonacionVistaMapper.formatearFecha(response.getFechaAsignacion()));

      context.render("donaciones/views/beneficiario/detalle_donacion.hbs", modelo);

    } catch (StatusRuntimeException e) {
      responderError(context, e);
    }
  }

  public void mostrarNovedades(Context context) {
    Usuario usuarioActual = beneficiarioAutenticado(context, "No tenés permiso para consultar notificaciones");
    if (usuarioActual == null) return;

    try {
      ListaNovedadesBeneficiarioResponse response = donacionesStub.listarNovedadesBeneficiario(
          BeneficiarioIdRequest.newBuilder()
              .setId(usuarioActual.getIdBeneficiario())
              .build()
      );

      // El microservicio ya las devuelve ordenadas de más reciente a más antigua
      List<Map<String, Object>> novedades = response.getNovedadesList().stream()
          .map(this::aVistaNovedad)
          .toList();

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("novedades", novedades);

      context.render("donaciones/views/beneficiario/notificaciones.hbs", modelo);

    } catch (StatusRuntimeException e) {
      responderError(context, e);
    }
  }

  //============= FUNCIONES AUXILIARES =====================
  private Usuario beneficiarioAutenticado(Context context, String mensajeSinPermiso) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return null;
    }
    if (!usuarioActual.isBeneficiario()) {
      context.status(403).result(mensajeSinPermiso);
      return null;
    }
    if (usuarioActual.getIdBeneficiario() == null) {
      context.status(404).result("No existe un beneficiario asociado a este usuario");
      return null;
    }
    return usuarioActual;
  }

  private void responderError(Context context, StatusRuntimeException e) {
    if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
      context.status(404).result(e.getStatus().getDescription());
    } else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
      context.status(400).result(e.getStatus().getDescription());
    } else {
      throw e;
    }
  }

  private Map<String, Object> aVistaDonacion(DonacionBeneficiarioMsg donacion) {
    Map<String, Object> datos = new HashMap<>();
    datos.put("id", donacion.getId());
    datos.put("descripcion", donacion.getDescripcion());
    datos.put("estadoActual", donacion.getEstadoActual());
    datos.put("fechaAsignacion", DonacionVistaMapper.formatearFecha(donacion.getFechaAsignacion()));
    return datos;
  }

  private Map<String, Object> aVistaNovedad(NovedadDonacionMsg novedad) {
    Map<String, Object> datos = new HashMap<>();
    datos.put("idDonacion", novedad.getIdDonacion());
    datos.put("descripcion", novedad.getDescripcion());
    datos.put("tipoEstado", novedad.getTipoEstado());
    datos.put("fecha", DonacionVistaMapper.formatearFechaHora(novedad.getFecha()));
    datos.put("detalle", novedad.getDetalle().isBlank() ? null : novedad.getDetalle());
    return datos;
  }
}