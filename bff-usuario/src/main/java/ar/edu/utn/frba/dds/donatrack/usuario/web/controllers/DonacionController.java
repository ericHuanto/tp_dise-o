package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.FiltroDonacionesAdminRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonacionesAdminResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.TipoEstadoDonacionProto;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.DonacionFila;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.Opcion;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.DonacionVistaMapper;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionController {
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

    ListaDonacionesAdminResponse response;
    try {
      response = donacionesStub.listarDonacionesAdmin(FiltroDonacionesAdminRequest.newBuilder()
          .setQ(q)
          .setCategoria(categoria)
          .setEstado(estado)
          .setVencimiento(vencimiento)
          .build());
    } catch (StatusRuntimeException e) {
      responderError(context, e);
      return;
    }

    LocalDate hoy = LocalDate.now();
    List<DonacionFila> filas = response.getDonacionesList().stream()
        .map(d -> DonacionVistaMapper.aFila(d, hoy))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("donaciones", filas);
    modelo.put("total", filas.size());
    modelo.put("q", q);
    modelo.put("categorias", opcionesCategoria(response.getCategoriasList(), categoria));
    modelo.put("estados", opcionesEstado(estado));
    modelo.put("vencimientos", seleccionar(VENCIMIENTOS, vencimiento));
    modelo.put("vencidaCodigo", context.queryParam("vencida_codigo"));
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

    context.redirect("/donaciones?vencida_codigo=" + DonacionVistaMapper.codigo(idDonacion));
  }

  //============= FUNCIONES AUXILIARES =====================

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