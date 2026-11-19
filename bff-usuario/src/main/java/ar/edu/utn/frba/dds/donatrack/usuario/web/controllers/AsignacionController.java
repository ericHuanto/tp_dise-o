package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.CandidatoDetalleMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.ConfirmarAsignacionRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaRankingsResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.NecesidadCubiertaMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.RankingDetalleResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.RankingIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.RankingVistaMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.DonacionVistaMapper;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionController {
  private final DonacionesServiceBlockingStub donacionesStub;

  public AsignacionController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarTodos(Context context) {
    ListaRankingsResponse response;
    try {
      response = donacionesStub.listarRankingsVigentes(Vacio.getDefaultInstance());
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    List<Map<String, Object>> rankings = response.getRankingsList().stream()
        .map(this::aFilaLista)
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("rankings", rankings);
    modelo.put("asignadaCodigo", context.queryParam("asignada"));

    context.render("donaciones/views/admin/asignaciones.hbs", modelo);
  }

  public void mostrarDetalle(Context context) {
    Long idRanking = parsearId(context.pathParam("id"));
    if (idRanking == null) {
      context.status(400).result("ID de ranking inválido");
      return;
    }

    RankingDetalleResponse ranking;
    try {
      ranking = donacionesStub.obtenerRanking(RankingIdRequest.newBuilder().setId(idRanking).build());
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    Map<String, Object> datosRanking = new HashMap<>();
    datosRanking.put("id", ranking.getId());
    datosRanking.put("fechaGeneracion", DonacionVistaMapper.formatearFechaHora(ranking.getFechaGeneracion()));

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("ranking", datosRanking);
    modelo.put("donacion", DonacionVistaMapper.aFila(ranking.getDonacion(), LocalDate.now()));
    modelo.put("candidatos", ranking.getCandidatosList().stream().map(this::aCandidatoVista).toList());
    modelo.put("pendiente", ranking.getVigente());
    modelo.put("beneficiariaAsignada", ranking.getBeneficiariaAsignada());

    context.render("donaciones/views/admin/asignar_beneficiaria.hbs", modelo);
  }

  public void confirmarAsignacion(Context context) {
    Long idRanking = parsearId(context.pathParam("id"));
    Long idBeneficiario = parsearId(context.formParam("id_beneficiario"));
    Long idDonacion = parsearId(context.formParam("id_donacion"));
    if (idRanking == null || idBeneficiario == null) {
      context.status(400).result("Elegí una entidad beneficiaria");
      return;
    }

    try {
      donacionesStub.confirmarAsignacion(
          ConfirmarAsignacionRequest.newBuilder()
              .setIdRanking(idRanking)
              .setIdBeneficiario(idBeneficiario)
              .build()
      );
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    String aviso = idDonacion == null ? "" : "?asignada=" + DonacionVistaMapper.codigo(idDonacion);
    context.redirect("/asignaciones" + aviso);
  }

  //============= FUNCIONES AUXILIARES =====================

  private Map<String, Object> aFilaLista(RankingVistaMsg r) {
    String estado = r.getDonacion().getEstado();

    Map<String, Object> donacion = new HashMap<>();
    donacion.put("codigo", DonacionVistaMapper.codigo(r.getDonacion().getId()));
    donacion.put("descripcion", r.getDonacion().getDescripcion());
    donacion.put("estadoClave", DonacionVistaMapper.estadoClave(estado));
    donacion.put("estadoTexto", DonacionVistaMapper.estadoTexto(estado));

    Map<String, Object> fila = new HashMap<>();
    fila.put("id", r.getId());
    fila.put("fechaGeneracion", DonacionVistaMapper.formatearFechaHora(r.getFechaGeneracion()));
    fila.put("cantCandidatos", r.getCandidatosCount());
    fila.put("donacion", donacion);
    return fila;
  }

  private Map<String, Object> aCandidatoVista(CandidatoDetalleMsg c) {
    Map<String, Object> candidato = new HashMap<>();
    candidato.put("posicion", c.getPosicion());
    candidato.put("posicionTexto", String.format("%02d", c.getPosicion()));
    candidato.put("idBeneficiario", c.getIdBeneficiario());
    candidato.put("razonSocial", c.getRazonSocial());
    candidato.put("direccion", c.getDireccion());
    candidato.put("necesidades", c.getNecesidadesList().stream().map(this::aNecesidadVista).toList());
    return candidato;
  }

  private Map<String, Object> aNecesidadVista(NecesidadCubiertaMsg n) {
    boolean extraordinaria = "EXTRAORDINARIA".equals(n.getTipo());
    Map<String, Object> necesidad = new HashMap<>();
    necesidad.put("descripcion", n.getDescripcion());
    necesidad.put("faltante", n.getFaltante());
    necesidad.put("unidad", n.getUnidadMedida().toLowerCase());
    necesidad.put("tipoTexto", extraordinaria ? "Extraordinaria" : "Recurrente");
    necesidad.put("tipoClave", extraordinaria ? "alta" : "baja");   // reutiliza las clases .urgencia-alta / .urgencia-baja
    return necesidad;
  }

  private Long parsearId(String valor) {
    try {
      return valor == null ? null : Long.parseLong(valor.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Traduce el Status gRPC que devuelve el microservicio al código HTTP equivalente. */
  private void responderError(Context context, StatusRuntimeException e) {
    String mensaje = e.getStatus().getDescription();
    switch (e.getStatus().getCode()) {
      case NOT_FOUND -> context.status(404).result(mensaje);
      case INVALID_ARGUMENT -> context.status(400).result(mensaje);
      case FAILED_PRECONDITION -> context.status(409).result(mensaje);
      default -> throw e;
    }
  }
}