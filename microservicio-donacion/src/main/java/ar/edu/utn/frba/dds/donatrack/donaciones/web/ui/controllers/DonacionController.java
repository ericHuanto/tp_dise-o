package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.vista.Opcion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.vista.DonacionFila;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.vista.DonacionVistaMapper;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionController implements WithSimplePersistenceUnit {
  private static final List<TipoEstadoDonacion> EN_INVENTARIO =
      List.of(TipoEstadoDonacion.EN_DEPOSITO, TipoEstadoDonacion.ASIGNACION_REALIZADA);
  private static final String VENCIMIENTO_POR_DEFECTO = "todos";
  private static final List<Opcion> VENCIMIENTOS = List.of(
      new Opcion("7_dias", "Próximos 7 días"),
      new Opcion("30_dias", "Próximos 30 días"),
      new Opcion("vencidas", "Vencidas"),
      new Opcion("no_perecedero", "No perecederos"),
      new Opcion("todos", "Todos"));

  private final DonacionRepository repoDonaciones;
  private final SubcategoriaRepository repoSubcategorias;

  public DonacionController(DonacionRepository donacionRepository,
                            SubcategoriaRepository subcategoriaRepository) {
    this.repoDonaciones = donacionRepository;
    this.repoSubcategorias = subcategoriaRepository;
  }

  public void mostrarDonaciones(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    String q = limpio(context.queryParam("q"));
    String categoria = limpio(context.queryParam("categoria"));
    String estado = limpio(context.queryParam("estado"));
    String vencimiento = limpio(context.queryParam("vencimiento"));
    if (vencimiento.isEmpty()) vencimiento = VENCIMIENTO_POR_DEFECTO;   // repuesta
    LocalDate hoy = LocalDate.now();                                     // repuesta

    List<Donacion> inventario = repoDonaciones.buscarTodos();

    String vencimientoFiltro = vencimiento;
    List<DonacionFila> filas = inventario.stream()
        .filter(d -> coincideBusqueda(d, q))
        .filter(d -> categoria.isEmpty() || coincideCategoria(d, categoria))
        .filter(d -> estado.isEmpty() || d.getEstadoActual().name().equalsIgnoreCase(estado))
        .filter(d -> coincideVencimiento(d, vencimientoFiltro, hoy))
        .sorted(Comparator
            .comparing(DonacionVistaMapper::vencimientoDe, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Donacion::getId))
        .map(d -> DonacionVistaMapper.aFila(d, hoy))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("donaciones", filas);
    modelo.put("total", filas.size());
    modelo.put("q", q);
    modelo.put("categorias", opcionesCategoria(categoria));
    modelo.put("estados", opcionesEstado(estado));   // nuevo
    modelo.put("vencimientos", seleccionar(VENCIMIENTOS, vencimiento));
    modelo.put("vencidaCodigo", context.queryParam("vencida_codigo"));
    modelo.put("creadas", context.queryParam("creadas"));

    context.render("views/admin/donaciones.hbs", modelo);
  }

  public void marcarComoVencida(Context context) {
    Long idDonacion = Long.parseLong(context.pathParam("id"));
    Donacion donacion = repoDonaciones.buscarPorId(idDonacion);

    donacion.vencida();

    beginTransaction();
    repoDonaciones.actualizar(donacion);
    commitTransaction();

    context.redirect("/donaciones?vencida_codigo=" + DonacionVistaMapper.codigo(idDonacion));
  }

  //============= FUNCIONES AUXILIARES =====================
  private boolean coincideBusqueda(Donacion donacion, String q) {
    if (q.isEmpty()) return true;
    String buscado = q.toLowerCase();
    return DonacionVistaMapper.codigo(donacion.getId()).toLowerCase().contains(buscado)
        || donacion.getBienes().stream().anyMatch(b -> b.getDescripcion().toLowerCase().contains(buscado))
        || donacion.getDonantes().stream().anyMatch(d -> d.getNombreCompleto().toLowerCase().contains(buscado));
  }

  private boolean coincideCategoria(Donacion donacion, String categoria) {
    return donacion.getBienes().stream()
        .anyMatch(b -> b.getSubcategoria().getCategoria().getNombre().equalsIgnoreCase(categoria));
  }

  private boolean coincideVencimiento(Donacion donacion, String filtro, LocalDate hoy) {
    LocalDate vence = DonacionVistaMapper.vencimientoDe(donacion);
    return switch (filtro) {
      case "7_dias" -> vence != null && !vence.isAfter(hoy.plusDays(7));
      case "30_dias" -> vence != null && !vence.isAfter(hoy.plusDays(30));
      case "vencidas" -> vence != null && vence.isBefore(hoy);
      case "no_perecedero" -> vence == null;
      case "todos" -> true;
      default -> throw new DominioException("El filtro de vencimiento: " + filtro + " no existe");
    };
  }

  private List<Opcion> opcionesCategoria(String seleccionada) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todas"));
    repoSubcategorias.buscarTodas().stream()
        .map(s -> s.getCategoria().getNombre())
        .distinct().sorted()
        .forEach(nombre -> opciones.add(new Opcion(nombre, DonacionVistaMapper.capitalizar(nombre))));
    return seleccionar(opciones, seleccionada);
  }

  private List<Opcion> seleccionar(List<Opcion> opciones, String valor) {
    return opciones.stream()
        .map(o -> new Opcion(o.getValor(), o.getTexto(), o.getValor().equalsIgnoreCase(valor)))
        .toList();
  }

  private List<Opcion> opcionesEstado(String seleccionado) {
    List<Opcion> opciones = new ArrayList<>();
    opciones.add(new Opcion("", "Todos"));
    for (TipoEstadoDonacion t : TipoEstadoDonacion.values()) {
      opciones.add(new Opcion(t.name(), DonacionVistaMapper.estadoTexto(t)));
    }
    return seleccionar(opciones, seleccionado);
  }

  private String limpio(String valor) {
    return valor == null ? "" : valor.trim();
  }
}