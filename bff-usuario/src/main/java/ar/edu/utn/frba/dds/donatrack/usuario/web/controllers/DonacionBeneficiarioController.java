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
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.Opcion;
import ar.edu.utn.frba.dds.donatrack.usuario.web.mappers.DonacionVistaMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionBeneficiarioController {

  private static final int TAMANO_PAGINA = 10;
  private static final List<Opcion> ESTADOS = List.of(
      new Opcion("", "Todas"),
      new Opcion("ASIGNACION_REALIZADA", "Asignada"),
      new Opcion("LISTA_PARA_ENTREGAR", "Preparando entrega"),
      new Opcion("EN_TRASLADO", "En traslado"),
      new Opcion("ENTREGADA", "Entregada"),
      new Opcion("ENTREGA_FALLIDA", "Entrega fallida"),
      new Opcion("EN_DEPOSITO", "En depósito"),
      new Opcion("VENCIDA", "Vencida")
  );

  private final DonacionesServiceBlockingStub donacionesStub;

  public DonacionBeneficiarioController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void listar(Context context) {
    Usuario usuarioActual = beneficiarioAutenticado(context, "No tenés permiso para consultar donaciones");
    if (usuarioActual == null) return;

    String q = limpio(context.queryParam("q"));
    String estado = limpio(context.queryParam("estado"));
    int paginaSolicitada = parsearPagina(context.queryParam("page"));

    try {
      ListaDonacionesBeneficiarioResponse response = donacionesStub.listarDonacionesPorBeneficiario(
          BeneficiarioIdRequest.newBuilder()
              .setId(usuarioActual.getIdBeneficiario())
              .build()
      );

      List<DonacionBeneficiarioMsg> todas = response.getDonacionesList();

      List<DonacionBeneficiarioMsg> filtradas = todas.stream()
          .filter(d -> estado.isEmpty() || estado.equalsIgnoreCase(d.getEstadoActual()))
          .filter(d -> q.isEmpty() || coincideBusqueda(d, q))
          .toList();

      int total = filtradas.size();
      int totalPaginas = Math.max(1, (int) Math.ceil((double) total / TAMANO_PAGINA));
      int paginaActual = Math.min(paginaSolicitada, totalPaginas);

      int desdeIdx = (paginaActual - 1) * TAMANO_PAGINA;
      int hastaIdx = Math.min(desdeIdx + TAMANO_PAGINA, total);

      var donacionesPagina = total == 0
          ? List.of()
          : filtradas.subList(desdeIdx, hastaIdx).stream()
            .map(this::aVistaDonacion)
            .toList();

      String basePath = context.path();

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("resumen", calcularResumen(todas));
      modelo.put("donaciones", donacionesPagina);
      modelo.put("total", total);
      modelo.put("desde", total == 0 ? 0 : desdeIdx + 1);
      modelo.put("hasta", hastaIdx);
      modelo.put("urlAnterior", paginaActual > 1 ? urlPagina(basePath, q, estado, paginaActual - 1) : null);
      modelo.put("urlSiguiente", paginaActual < totalPaginas ? urlPagina(basePath, q, estado, paginaActual + 1) : null);
      modelo.put("q", q);
      modelo.put("estados", seleccionar(ESTADOS, estado));
      modelo.put("hayFiltros", !q.isEmpty() || !estado.isEmpty());
      modelo.put("urlLimpiar", basePath);

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
      var novedades = response.getNovedadesList().stream()
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
    String estado = donacion.getEstadoActual();
    Map<String, Object> datos = new HashMap<>();
    datos.put("id", donacion.getId());
    datos.put("codigo", "#" + donacion.getId());
    datos.put("descripcion", donacion.getDescripcion());
    datos.put("estadoActual", estado);
    datos.put("estadoTexto", "LISTA_PARA_ENTREGAR".equals(estado) ? "Preparando entrega" : DonacionVistaMapper.estadoTexto(estado));
    datos.put("estadoClave", DonacionVistaMapper.estadoClave(estado));
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

  private Map<String, Long> calcularResumen(List<DonacionBeneficiarioMsg> entregas) {
    Map<String, Long> resumen = new HashMap<>();
    resumen.put("total", (long) entregas.size());
    resumen.put("asignadas", entregas.stream().filter(d -> "ASIGNACION_REALIZADA".equals(d.getEstadoActual())).count());
    resumen.put("entregadas", entregas.stream().filter(d -> "ENTREGADA".equals(d.getEstadoActual())).count());
    resumen.put("fallidas", entregas.stream().filter(d -> "ENTREGA_FALLIDA".equals(d.getEstadoActual())).count());
    resumen.put("enCurso", entregas.stream().filter(d -> {
      String est = d.getEstadoActual();
      return "ASIGNACION_REALIZADA".equals(est)
          || "LISTA_PARA_ENTREGAR".equals(est)
          || "EN_TRASLADO".equals(est)
          || "EN_DEPOSITO".equals(est);
    }).count());
    return resumen;
  }

  private boolean coincideBusqueda(DonacionBeneficiarioMsg donacion, String q) {
    String termino = q.toLowerCase();
    String texto = (
        donacion.getId() + " " +
            DonacionVistaMapper.codigo(donacion.getId()) + " " +
            DonacionVistaMapper.formatearFecha(donacion.getFechaAsignacion()) + " " +
            donacion.getDescripcion()
    ).toLowerCase();
    return texto.contains(termino);
  }

  private List<Opcion> seleccionar(List<Opcion> opciones, String valor) {
    return opciones.stream()
        .map(o -> new Opcion(o.getValor(), o.getTexto(), o.getValor().equalsIgnoreCase(valor)))
        .toList();
  }

  private int parsearPagina(String valor) {
    if (valor == null || valor.isBlank()) return 1;
    try {
      return Math.max(1, Integer.parseInt(valor.trim()));
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  private String urlPagina(String basePath, String q, String estado, int page) {
    StringBuilder url = new StringBuilder(basePath).append("?page=").append(page);
    if (!q.isEmpty()) url.append("&q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
    if (!estado.isEmpty()) url.append("&estado=").append(URLEncoder.encode(estado, StandardCharsets.UTF_8));
    return url.toString();
  }

  private String limpio(String valor) {
    return valor == null ? "" : valor.trim();
  }
}