package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionAdminFilaMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.FiltroDonacionesAdminRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonacionesAdminResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.TipoEstadoDonacionProto;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.DonacionFila;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.Opcion;
import ar.edu.utn.frba.dds.donatrack.usuario.web.mappers.DonacionVistaMapper;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DonacionController {
  private static final int TAMANO_PAGINA = 10;
  private static final String VENCIMIENTO_POR_DEFECTO = "todos";
  private static final List<Opcion> VENCIMIENTOS = List.of(
      new Opcion("7_dias", "Próximos 7 días"),
      new Opcion("30_dias", "Próximos 30 días"),
      new Opcion("vencidas", "Vencidas"),
      new Opcion("no_perecedero", "No perecederos"),
      new Opcion("todos", "Todos")
  );

  private final DonacionesServiceBlockingStub donacionesStub;

  public DonacionController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarDonaciones(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    String q = limpio(context.queryParam("q"));
    String categoria = limpio(context.queryParam("categoria"));
    String estado = limpio(context.queryParam("estado"));
    String vencimiento = limpio(context.queryParam("vencimiento"));
    if (vencimiento.isEmpty()) {
      vencimiento = VENCIMIENTO_POR_DEFECTO;
    }
    int paginaSolicitada = parsearPagina(context.queryParam("page"));

    ListaDonacionesAdminResponse response;
    ListaDonacionesAdminResponse responseGlobal;
    try {
      response = donacionesStub.listarDonacionesAdmin(FiltroDonacionesAdminRequest.newBuilder()
          .setQ(q)
          .setCategoria(categoria)
          .setEstado(estado)
          .setVencimiento(vencimiento)
          .build());

      boolean hayFiltros = !q.isEmpty() || !categoria.isEmpty() || !estado.isEmpty()
          || !VENCIMIENTO_POR_DEFECTO.equals(vencimiento);
      responseGlobal = hayFiltros
          ? donacionesStub.listarDonacionesAdmin(FiltroDonacionesAdminRequest.newBuilder()
                                                 .setVencimiento(VENCIMIENTO_POR_DEFECTO)
                                                 .build())
          : response;
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    LocalDate hoy = LocalDate.now();
    List<DonacionFila> todas = response.getDonacionesList().stream()
        .map(d -> DonacionVistaMapper.aFila(d, hoy))
        .toList();

    int total = todas.size();
    int totalPaginas = Math.max(1, (int) Math.ceil((double) total / TAMANO_PAGINA));
    int paginaActual = Math.min(paginaSolicitada, totalPaginas);

    int desdeIdx = (paginaActual - 1) * TAMANO_PAGINA;
    int hastaIdx = Math.min(desdeIdx + TAMANO_PAGINA, total);

    List<DonacionFila> filasPagina = total == 0 ? List.of() : todas.subList(desdeIdx, hastaIdx);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("resumen", calcularResumen(responseGlobal.getDonacionesList()));
    modelo.put("donaciones", filasPagina);
    modelo.put("total", total);
    modelo.put("desde", total == 0 ? 0 : desdeIdx + 1);
    modelo.put("hasta", hastaIdx);
    modelo.put("urlAnterior", paginaActual > 1 ? urlPagina(q, categoria, estado, vencimiento, paginaActual - 1) : null);
    modelo.put("urlSiguiente", paginaActual < totalPaginas ? urlPagina(q, categoria, estado, vencimiento, paginaActual + 1) : null);
    modelo.put("q", q);
    modelo.put("categorias", opcionesCategoria(response.getCategoriasList(), categoria));
    modelo.put("estados", opcionesEstado(estado));
    modelo.put("vencimientos", seleccionar(VENCIMIENTOS, vencimiento));
    modelo.put("vencidaCodigo", context.consumeSessionAttribute("vencida_codigo"));
    modelo.put("creadas", context.queryParam("creadas"));

    context.render("donaciones/views/admin/donaciones.hbs", modelo);
  }

  public void marcarComoVencida(Context context) {
    long idDonacion;
    try {
      idDonacion = Long.parseLong(context.pathParam("id"));
    } catch (NumberFormatException e) {
      context.status(400).result("ID de donación inválido");
      return;
    }

    try {
      donacionesStub.marcarDonacionComoVencida(DonacionId.newBuilder().setId(idDonacion).build());
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    context.sessionAttribute("vencida_codigo", DonacionVistaMapper.codigo(idDonacion));
    context.redirect("/donaciones");
  }

  //============= FUNCIONES AUXILIARES =====================

  private Map<String, Long> calcularResumen(List<DonacionAdminFilaMsg> donaciones) {
    Map<String, Long> resumen = new HashMap<>();
    resumen.put("pendientes", contarEstado(donaciones, "EN_DEPOSITO"));
    resumen.put("asignadas", contarEstado(donaciones, "ASIGNACION_REALIZADA"));
    resumen.put("listasParaEntregar", contarEstado(donaciones, "LISTA_PARA_ENTREGAR"));
    resumen.put("enTraslado", contarEstado(donaciones, "EN_TRASLADO"));
    resumen.put("entregadas", contarEstado(donaciones, "ENTREGADA"));
    resumen.put("entregaFallida", contarEstado(donaciones, "ENTREGA_FALLIDA"));
    resumen.put("vencidas", contarEstado(donaciones, "VENCIDA"));
    return resumen;
  }

  private long contarEstado(List<DonacionAdminFilaMsg> donaciones, String estado) {
    return donaciones.stream()
        .filter(d -> estado.equals(d.getEstadoActual()))
        .count();
  }

  private int parsearPagina(String valor) {
    if (valor == null || valor.isBlank()) return 1;
    try {
      return Math.max(1, Integer.parseInt(valor.trim()));
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  private String urlPagina(String q, String categoria, String estado, String vencimiento, int page) {
    StringBuilder url = new StringBuilder("/donaciones?page=").append(page);
    if (!q.isEmpty()) url.append("&q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
    if (!categoria.isEmpty()) url.append("&categoria=").append(URLEncoder.encode(categoria, StandardCharsets.UTF_8));
    if (!estado.isEmpty()) url.append("&estado=").append(URLEncoder.encode(estado, StandardCharsets.UTF_8));
    if (!vencimiento.isEmpty()) url.append("&vencimiento=").append(URLEncoder.encode(vencimiento, StandardCharsets.UTF_8));
    return url.toString();
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

  private List<Opcion> opcionesCategoria(List<String> categorias, String seleccionada) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todas"));
    categorias.forEach(nombre -> opciones.add(new Opcion(nombre, DonacionVistaMapper.capitalizar(nombre))));
    return seleccionar(opciones, seleccionada);
  }

  private List<Opcion> opcionesEstado(String seleccionado) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todos"));
    for (TipoEstadoDonacionProto t : TipoEstadoDonacionProto.values()) {
      if (t == TipoEstadoDonacionProto.UNRECOGNIZED) continue;
      opciones.add(new Opcion(t.name(), DonacionVistaMapper.estadoTexto(t.name())));
    }
    return seleccionar(opciones, seleccionado);
  }

  private List<Opcion> seleccionar(List<Opcion> opciones, String valor) {
    return opciones.stream()
        .map(o -> new Opcion(o.getValor(), o.getTexto(), o.getValor().equalsIgnoreCase(valor)))
        .toList();
  }

  private String limpio(String valor) {
    return valor == null ? "" : valor.trim();
  }
}