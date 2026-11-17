package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.vista;

import static ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion.ASIGNACION_REALIZADA;
import static ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion.EN_DEPOSITO;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.Perecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.vista.DonacionFila;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public final class DonacionVistaMapper {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final long DIAS_PRONTO = 7;

  private DonacionVistaMapper() {}

  public static String codigo(Long id) {
    return String.format("D-%04d", id);
  }

  public static String capitalizar(String texto) {
    return texto == null || texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
  }

  /** Los bienes de una donación comparten fecha por el segmentador; igual tomamos la más próxima. */
  public static LocalDate vencimientoDe(Donacion donacion) {
    return donacion.getBienes().stream()
        .filter(Perecedero.class::isInstance)
        .map(b -> ((Perecedero) b).getFechaVencimiento())
        .min(Comparator.naturalOrder())
        .orElse(null);
  }

  public static DonacionFila aFila(Donacion donacion, LocalDate hoy) {
    LocalDate vence = vencimientoDe(donacion);
    long dias = vence == null ? 0 : ChronoUnit.DAYS.between(hoy, vence);
    TipoEstadoDonacion estado = donacion.getEstadoActual();

    return DonacionFila.builder()
        .id(donacion.getId())
        .codigo(codigo(donacion.getId()))
        .donantes(donacion.getDonantes().stream().map(DonacionVistaMapper::nombreDe).toList())
        .fechaIngreso(donacion.getHistorialEstados().get(0).getFecha().toLocalDate().format(FECHA))
        .bienes(donacion.getBienes().stream().map(DonacionVistaMapper::aBienFila).toList())
        .vencimiento(vence == null ? null : vence.format(FECHA))
        .vencimientoTexto(vence == null ? null : textoVencimiento(dias))
        .vencimientoNivel(vence == null ? null : dias < 0 ? "vencido" : dias < DIAS_PRONTO ? "pronto" : "normal")
        .estadoClave(estadoClave(estado))
        .estadoTexto(estadoTexto(estado))
        .vencible(donacion.sePuedeMarcarVencida())
        .build();
  }

  private static DonacionFila.BienFila aBienFila(Bien bien) {
    float cantidad = bien.getCantidad();
    return DonacionFila.BienFila.builder()
        .descripcion(bien.getDescripcion())
        .categoria(capitalizar(bien.getSubcategoria().getCategoria().getNombre()))
        .subcategoria(capitalizar(bien.getSubcategoria().getNombre()))
        .cantidad(cantidad == Math.rint(cantidad) ? String.valueOf((long) cantidad) : String.valueOf(cantidad))
        .unidadMedida(bien.getUnidadMedida().toString().toLowerCase())
        .build();
  }

  private static String nombreDe(Donante donante) {
    return donante.getNombreCompleto();
  }

  private static String textoVencimiento(long dias) {
    if (dias < 0) return "Vencido hace " + (-dias) + (dias == -1 ? " día" : " días");
    if (dias == 0) return "Vence hoy";
    return "Vence en " + dias + (dias == 1 ? " día" : " días");
  }

  private static String estadoClave(TipoEstadoDonacion estado) {
    return switch (estado) {
      case EN_DEPOSITO -> "pendiente";
      case ASIGNACION_REALIZADA -> "asignada";
      default -> estado.name().toLowerCase();
    };
  }

  public static String estadoTexto(TipoEstadoDonacion estado) {
    return switch (estado) {
      case EN_DEPOSITO -> "Pendiente de asignación";
      case ASIGNACION_REALIZADA -> "Asignada";
      case LISTA_PARA_ENTREGAR -> "Lista para entregar";
      case EN_TRASLADO -> "En traslado";
      case ENTREGADA -> "Entregada";
      case ENTREGA_FALLIDA -> "Entrega fallida";
      case VENCIDA -> "Vencida";
    };
  }
}