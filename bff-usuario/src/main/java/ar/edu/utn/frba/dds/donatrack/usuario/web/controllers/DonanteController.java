package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.BeneficiarioTarjeta;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.CamionSeguimiento;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.DonacionDonanteFila;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.FiltroActivo;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.GrupoCategoria;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.NecesidadFila;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.Opcion;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.ResumenDonaciones;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.BeneficiarioVistaMapper;
import ar.edu.utn.frba.dds.donatrack.usuario.web.mappers.DonacionVistaMapper;
import com.google.gson.Gson;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DonanteController {
  private static final String RUTA_DONACIONES = "/donante/donaciones";
  private static final int DONACIONES_POR_PAGINA = 8;

  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;
  private final LogisticaServiceGrpc.LogisticaServiceBlockingStub logisticaStub;

  public DonanteController(DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub,
                           LogisticaServiceGrpc.LogisticaServiceBlockingStub logisticaStub) {
    this.donacionesStub = donacionesStub;
    this.logisticaStub = logisticaStub;
  }

  public void donacionesPropias(Context context) {
    Long idDonante = idDonanteLogueado(context);
    if (idDonante == null) {
      return;
    }

    String estado = limpio(context.queryParam("estado"));
    String q = limpio(context.queryParam("q"));
    Long idCategoria;
    List<Long> idsSubcategorias;
    int pagina;
    try {
      String categoriaParam = limpio(context.queryParam("categoria"));
      idCategoria = categoriaParam.isEmpty() ? null : Long.valueOf(categoriaParam);
      idsSubcategorias = ids(context.queryParams("subcategoria"));
      String paginaParam = limpio(context.queryParam("pagina"));
      pagina = paginaParam.isEmpty() ? 1 : Math.max(1, Integer.parseInt(paginaParam));
    } catch (NumberFormatException e) {
      context.redirect(RUTA_DONACIONES);
      return;
    }

    boolean filtraEnMicroservicio = !estado.isEmpty() || idCategoria != null || !idsSubcategorias.isEmpty();
    VistaDonacionesDonanteResponse response;
    VistaDonacionesDonanteResponse sinFiltros;
    try {
      response = donacionesStub.obtenerVistaDonacionesDonante(FiltroDonacionesDonanteRequest.newBuilder()
          .setIdDonante(idDonante)
          .setEstado(estado)
          .setIdCategoria(idCategoria != null ? idCategoria : 0L)
          .addAllIdsSubcategorias(idsSubcategorias)
          .build());
      sinFiltros = filtraEnMicroservicio
          ? donacionesStub.obtenerVistaDonacionesDonante(FiltroDonacionesDonanteRequest.newBuilder()
              .setIdDonante(idDonante)
              .build())
          : response;
    } catch (StatusRuntimeException e) {
      context.redirect("/");
      return;
    }

    List<DonacionDonanteFila> filtradas = response.getDonacionesList().stream()
        .map(DonacionVistaMapper::aFilaDonante)
        .filter(fila -> coincideBusqueda(fila, q))
        .toList();

    int total = filtradas.size();
    int totalPaginas = Math.max(1, (total + DONACIONES_POR_PAGINA - 1) / DONACIONES_POR_PAGINA);
    if (pagina > totalPaginas) {
      context.redirect(urlSin(context, "pagina", null));
      return;
    }
    int desde = (pagina - 1) * DONACIONES_POR_PAGINA;
    int hasta = Math.min(desde + DONACIONES_POR_PAGINA, total);
    List<DonacionDonanteFila> donaciones = filtradas.subList(desde, hasta);

    List<FiltroActivo> filtrosActivos = filtrosActivos(context, response, estado, q, idCategoria, idsSubcategorias);
    int subcategoriasElegidas = idsSubcategorias.size();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("resumen", new ResumenDonaciones(sinFiltros.getDonacionesList().stream()
        .map(DonacionVistaMsg::getEstadoActual)
        .toList()));
    modelo.put("donaciones", donaciones);
    modelo.put("total", total);
    modelo.put("desde", total == 0 ? 0 : desde + 1);
    modelo.put("hasta", hasta);
    modelo.put("urlAnterior", pagina > 1 ? urlPagina(context, pagina - 1) : null);
    modelo.put("urlSiguiente", pagina < totalPaginas ? urlPagina(context, pagina + 1) : null);
    modelo.put("q", q);
    modelo.put("estados", opcionesEstado(response.getEstadosList(), estado));
    modelo.put("categorias", opcionesCategoria(response.getCategoriasList(), idCategoria));
    modelo.put("gruposCategoria", gruposCategoria(response, idCategoria, idsSubcategorias));
    modelo.put("subcategoriasElegidas", subcategoriasElegidas);
    modelo.put("textoSubcategorias", subcategoriasElegidas == 0 ? "Subcategorías: Todas"
        : "Subcategorías · " + subcategoriasElegidas + (subcategoriasElegidas == 1 ? " seleccionada" : " seleccionadas"));
    modelo.put("filtrosActivos", filtrosActivos);
    modelo.put("hayFiltros", !filtrosActivos.isEmpty());

    context.render("donaciones/views/donante/donaciones_donante.hbs", modelo);
  }

  public void beneficiarios(Context context) {
    if (idDonanteLogueado(context) == null) {
      return;
    }

    String q = limpio(context.queryParam("q"));

    ListaBeneficiariosParaDonanteResponse response;
    try {
      response = donacionesStub.listarBeneficiariosParaDonante(Vacio.getDefaultInstance());
    } catch (StatusRuntimeException e) {
      context.redirect("/");
      return;
    }

    String buscado = q.toLowerCase();
    List<BeneficiarioTarjeta> beneficiarios = response.getBeneficiariosList().stream()
        .filter(b -> buscado.isEmpty()
            || b.getRazonSocial().toLowerCase().contains(buscado)
            || b.getDireccion().toLowerCase().contains(buscado))
        .map(BeneficiarioVistaMapper::aTarjeta)
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiarios", beneficiarios);
    modelo.put("q", q);
    modelo.put("total", response.getBeneficiariosCount());
    modelo.put("encontradas", beneficiarios.size());

    context.render("donaciones/views/donante/beneficiarios.hbs", modelo);
  }

  public void detalleBeneficiario(Context context) {
    if (idDonanteLogueado(context) == null) {
      return;
    }

    Long id;
    try {
      id = context.pathParamAsClass("id", Long.class).get();
    } catch (ValidationException e) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    DetalleBeneficiarioResponse response;
    try {
      response = donacionesStub.obtenerDetalleBeneficiario(BeneficiarioIdRequest.newBuilder().setId(id).build());
    } catch (StatusRuntimeException e) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    if (!response.getEncontrado()) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    BeneficiarioDetalleMsg b = response.getBeneficiario();
    List<NecesidadFila> necesidades = b.getNecesidadesList().stream()
        .map(BeneficiarioVistaMapper::aFila)
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiario", BeneficiarioVistaMapper.aTarjeta(b));
    modelo.put("necesidades", necesidades);

    context.render("donaciones/views/donante/detalle_beneficiario.hbs", modelo);
  }

  //================== FUNCIONES AUXILIARES ========================
  private List<Opcion> opcionesEstado(List<String> estados, String elegido) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todos", elegido.isEmpty()));
    estados.forEach(e -> opciones.add(
        new Opcion(e, DonacionVistaMapper.estadoTexto(e), e.equals(elegido))));
    return opciones;
  }

  private List<Opcion> opcionesCategoria(List<CategoriaMsg> categorias, Long elegida) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todas", elegida == null));
    categorias.forEach(c -> opciones.add(new Opcion(
        String.valueOf(c.getId()), DonacionVistaMapper.capitalizar(c.getNombre()), Long.valueOf(c.getId()).equals(elegida))));
    return opciones;
  }

  private List<GrupoCategoria> gruposCategoria(VistaDonacionesDonanteResponse response,
                                               Long idCategoria, List<Long> idsSubcategorias) {
    return response.getCategoriasList().stream()
        .filter(c -> idCategoria == null || c.getId() == idCategoria)
        .map(c -> new GrupoCategoria(
            DonacionVistaMapper.capitalizar(c.getNombre()),
            response.getSubcategoriasList().stream()
                .filter(s -> s.getCategoria().getId() == c.getId())
                .map(s -> new Opcion(String.valueOf(s.getId()), DonacionVistaMapper.capitalizar(s.getNombre()),
                    idsSubcategorias.contains(s.getId())))
                .toList()))
        .filter(g -> !g.getSubcategorias().isEmpty())
        .toList();
  }

  private List<FiltroActivo> filtrosActivos(Context context, VistaDonacionesDonanteResponse response, String estado,
                                            String q, Long idCategoria, List<Long> idsSubcategorias) {
    List<FiltroActivo> filtros = new ArrayList<>();
    if (!q.isEmpty()) {
      filtros.add(new FiltroActivo("Búsqueda: “" + q + "”", urlSin(context, "q", null)));
    }
    if (!estado.isEmpty()) {
      filtros.add(new FiltroActivo("Estado: " + DonacionVistaMapper.estadoTexto(estado), urlSin(context, "estado", null)));
    }
    response.getCategoriasList().stream()
        .filter(c -> idCategoria != null && c.getId() == idCategoria)
        .forEach(c -> filtros.add(new FiltroActivo(
            "Categoría: " + DonacionVistaMapper.capitalizar(c.getNombre()),
            urlSin(context, "categoria", null))));
    response.getSubcategoriasList().stream()
        .filter(s -> idsSubcategorias.contains(s.getId()))
        .forEach(s -> filtros.add(new FiltroActivo(
            DonacionVistaMapper.capitalizar(s.getCategoria().getNombre()) + " › "
                + DonacionVistaMapper.capitalizar(s.getNombre()),
            urlSin(context, "subcategoria", String.valueOf(s.getId())))));
    return filtros;
  }

  private boolean coincideBusqueda(DonacionDonanteFila fila, String q) {
    if (q.isEmpty()) return true;
    String buscado = q.toLowerCase();
    return fila.getCodigo().toLowerCase().contains(buscado)
        || fila.getDescripcion().toLowerCase().contains(buscado)
        || fila.getCategoria().toLowerCase().contains(buscado)
        || fila.getSubcategoria().toLowerCase().contains(buscado);
  }

  private String urlSin(Context context, String parametro, String valor) {
    Map<String, List<String>> params = new LinkedHashMap<>(context.queryParamMap());
    params.remove("pagina");
    if (valor == null) {
      params.remove(parametro);
    } else if (params.containsKey(parametro)) {
      params.put(parametro, params.get(parametro).stream().filter(v -> !v.equals(valor)).toList());
    }
    return armarUrl(params);
  }

  private String urlPagina(Context context, int pagina) {
    Map<String, List<String>> params = new LinkedHashMap<>(context.queryParamMap());
    params.put("pagina", List.of(String.valueOf(pagina)));
    return armarUrl(params);
  }

  private String armarUrl(Map<String, List<String>> params) {
    String query = params.entrySet().stream()
        .flatMap(e -> e.getValue().stream()
            .filter(v -> !v.isBlank())
            .map(v -> codificar(e.getKey()) + "=" + codificar(v)))
        .collect(Collectors.joining("&"));
    return query.isEmpty() ? RUTA_DONACIONES : RUTA_DONACIONES + "?" + query;
  }

  private String codificar(String valor) {
    return URLEncoder.encode(valor, StandardCharsets.UTF_8);
  }

  private List<Long> ids(List<String> valores) {
    return valores.stream().filter(v -> !v.isBlank()).map(Long::valueOf).toList();
  }

  private String limpio(String valor) {
    return valor == null ? "" : valor.trim();
  }

  private Long idDonanteLogueado(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.redirect("/login");
      return null;
    }
    if (usuarioActual.getIdDonante() == null) {
      context.redirect("/");
      return null;
    }
    return usuarioActual.getIdDonante();
  }
}