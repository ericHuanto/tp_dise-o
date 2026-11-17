package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista.Opcion;
import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;


public class RegistroEntregaController implements WithSimplePersistenceUnit {
  private final DonacionesServiceBlockingStub donacionesStub;

  public RegistroEntregaController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarFormulario(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    ListaSubcategoriasResponse response = donacionesStub.listarSubcategorias(Vacio.getDefaultInstance());

    Map<String, List<String>> catalogo = new TreeMap<>();
    for(SubcategoriaMsg s : response.getSubcategoriasList()) {
      catalogo.computeIfAbsent(s.getCategoria().getNombre(), k -> new ArrayList<>()).add(s.getNombre());
    }
    catalogo.values().forEach(Collections::sort);

    List<Map<String, Object>> categorias = catalogo.entrySet().stream()
        .map(e -> Map.<String, Object>of("nombre", e.getKey(), "subcategorias", e.getValue()))
        .toList();

    List<Opcion> unidades = Arrays.stream(UnidadMedidaProto.values())
        .filter(u -> u != UnidadMedidaProto.UNRECOGNIZED)
        .map(u -> new Opcion(u.name(), u.toString().toLowerCase()))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("hoy", LocalDate.now().toString());
    modelo.put("categorias", categorias);
    modelo.put("unidades", unidades);

    context.render("donaciones/views/admin/registrar_entrega.hbs", modelo);
  }

  // GET /api/donantes?q=...  (lo usa el buscador de la vista, vía fetch)
  public void buscarDonantes(Context context) {
    if (!esAdmin(context)) return;

    String q = context.queryParam("q");
    try {
      ListaDonantesResumenResponse response = donacionesStub.buscarDonantes(
          BuscarDonantesRequest.newBuilder().setQ(q == null ? "" : q.trim()).build());

      List<Map<String, Object>> donantes = response.getDonantesList().stream()
          .map(d -> Map.<String, Object>of(
              "id", d.getId(),
              "nombre", d.getNombre(),
              "documento", d.getDocumento(),
              "tipo_persona", d.getTipoPersona()))
          .toList();

      context.json(donantes);
    } catch (StatusRuntimeException e) {
      responderErrorJson(context, e);
    }
  }

  // POST /api/registros-entrega  (lo usa el submit de la vista, vía fetch con JSON)
  public void registrarEntrega(Context context) {
    if (!esAdmin(context)) return;

    RegistroEntregaBody body;
    try {
      body = context.bodyAsClass(RegistroEntregaBody.class);
    } catch (Exception e) {
      context.status(400).json(Map.of("mensaje", "El cuerpo de la solicitud no es válido"));
      return;
    }
    if (body == null || body.donanteId() == null) {
      context.status(400).json(Map.of("mensaje", "Elegí una persona donante"));
      return;
    }

    RegistrarEntregaRequest.Builder request = RegistrarEntregaRequest.newBuilder()
        .setIdDonante(body.donanteId())
        .setFechaRecepcion(nvl(body.fechaRecepcion()))
        .setObservaciones(nvl(body.observaciones()));

    if (body.bienes() != null) {
      for (BienBody b : body.bienes()) {
        request.addBienes(BienEntregaMsg.newBuilder()
            .setTipo(nvl(b.tipo()))
            .setCategoria(nvl(b.categoria()))
            .setSubcategoria(nvl(b.subcategoria()))
            .setDescripcion(nvl(b.descripcion()))
            .setCantidad(b.cantidad() == null ? 0f : b.cantidad())
            .setUnidadMedida(nvl(b.unidadMedida()))
            .setFechaVencimiento(nvl(b.fechaVencimiento()))
            .setUsado(Boolean.TRUE.equals(b.usado()))
            .build());
      }
    }

    try {
      RegistrarEntregaResponse response = donacionesStub.registrarEntrega(request.build());
      context.status(201).json(Map.of(
          "id", response.getIdRegistro(),
          "cantidad_donaciones", response.getCantidadDonaciones()));
    } catch (StatusRuntimeException e) {
      responderErrorJson(context, e);
    }
  }

  //============= FUNCIONES AUXILIARES =====================

  // Cuerpo JSON que manda la vista
  public record RegistroEntregaBody(Long donanteId, String fechaRecepcion, String observaciones,
                                    List<BienBody> bienes) {}

  public record BienBody(String tipo, String categoria, String subcategoria, String descripcion,
                         Float cantidad, String unidadMedida, String fechaVencimiento, Boolean usado) {}

  private boolean esAdmin(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.status(401).json(Map.of("mensaje", "Tenés que iniciar sesión"));
      return false;
    }
    if (!usuarioActual.isAdmin()) {
      context.status(403).json(Map.of("mensaje", "No tenés permiso para registrar entregas"));
      return false;
    }
    return true;
  }

  private void responderErrorJson(Context context, StatusRuntimeException e) {
    Status.Code codigo = e.getStatus().getCode();
    String mensaje = e.getStatus().getDescription() != null
        ? e.getStatus().getDescription()
        : "Error al comunicarse con el servicio de donaciones";
    int http = switch (codigo) {
      case INVALID_ARGUMENT -> 400;
      case NOT_FOUND -> 404;
      case FAILED_PRECONDITION -> 409;
      case UNAVAILABLE -> 503;
      default -> 500;
    };
    context.status(http).json(Map.of("mensaje", mensaje));
  }

  private String nvl(String valor) {
    return valor == null ? "" : valor;
  }
}