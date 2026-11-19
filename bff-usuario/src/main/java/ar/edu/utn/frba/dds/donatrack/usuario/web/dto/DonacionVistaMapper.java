package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import ar.edu.utn.frba.dds.donatrack.grpc.BienFilaMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionAdminFilaMsg;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class DonacionVistaMapper {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final String SIN_DATO = "—";
  private static final long DIAS_PRONTO = 7;

  private DonacionVistaMapper() {}

  public static String codigo(long id) {
    return String.format("D-%04d", id);
  }

  public static String capitalizar(String texto) {
    return texto == null || texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
  }

  public static DonacionFila aFila(DonacionAdminFilaMsg msg, LocalDate hoy) {
    LocalDate vence = parsearFecha(msg.getVencimiento());
    long dias = vence == null ? 0 : ChronoUnit.DAYS.between(hoy, vence);

    return DonacionFila.builder()
        .id(msg.getId())
        .codigo(codigo(msg.getId()))
        .donantes(msg.getDonantesList())
        .fechaIngreso(formatearFecha(msg.getFechaIngreso()))
        .bienes(msg.getBienesList().stream().map(DonacionVistaMapper::aBienFila).toList())
        .vencimiento(vence == null ? null : vence.format(FECHA))
        .vencimientoTexto(vence == null ? null : textoVencimiento(dias))
        .vencimientoNivel(vence == null ? null : dias < 0 ? "vencido" : dias < DIAS_PRONTO ? "pronto" : "normal")
        .estadoClave(estadoClave(msg.getEstadoActual()))
        .estadoTexto(estadoTexto(msg.getEstadoActual()))
        .vencible(msg.getVencible())
        .build();
  }

  public static String estadoTexto(String estado) {
    return switch (estado) {
      case "EN_DEPOSITO" -> "Pendiente de asignación";
      case "ASIGNACION_REALIZADA" -> "Asignada";
      case "LISTA_PARA_ENTREGAR" -> "Lista para entregar";
      case "EN_TRASLADO" -> "En traslado";
      case "ENTREGADA" -> "Entregada";
      case "ENTREGA_FALLIDA" -> "Entrega fallida";
      case "VENCIDA" -> "Vencida";
      default -> estado;
    };
  }

  public static String formatearFecha(String iso) {
    LocalDate fecha = parsearFecha(iso);
    return fecha == null ? SIN_DATO : fecha.format(FECHA);
  }

  public static String formatearFechaHora(String iso) {
    if (iso == null || iso.isBlank()) return SIN_DATO;
    try {
      return LocalDateTime.parse(iso).format(FECHA_HORA);
    } catch (DateTimeParseException e) {
      return iso;
    }
  }

  private static LocalDate parsearFecha(String iso) {
    if (iso == null || iso.isBlank()) return null;
    try {
      return LocalDate.parse(iso);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static DonacionFila.BienFila aBienFila(BienFilaMsg bien) {
    float cantidad = bien.getCantidad();
    return DonacionFila.BienFila.builder()
        .descripcion(bien.getDescripcion())
        .categoria(capitalizar(bien.getCategoria()))
        .subcategoria(capitalizar(bien.getSubcategoria()))
        .cantidad(cantidad == Math.rint(cantidad) ? String.valueOf((long) cantidad) : String.valueOf(cantidad))
        .unidadMedida(bien.getUnidadMedida().toLowerCase())
        .build();
  }

  private static String textoVencimiento(long dias) {
    if (dias < 0) return "Vencido hace " + (-dias) + (dias == -1 ? " día" : " días");
    if (dias == 0) return "Vence hoy";
    return "Vence en " + dias + (dias == 1 ? " día" : " días");
  }

  public static String estadoClave(String estado) {
    return switch (estado) {
      case "EN_DEPOSITO" -> "pendiente";
      case "ASIGNACION_REALIZADA" -> "asignada";
      default -> estado.toLowerCase();
    };
  }
}
