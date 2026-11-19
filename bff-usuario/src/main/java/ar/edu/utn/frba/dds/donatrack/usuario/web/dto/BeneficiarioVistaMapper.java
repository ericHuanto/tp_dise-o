package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioDetalleMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.NecesidadVistaMsg;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista.BeneficiarioTarjeta;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista.NecesidadFila;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class BeneficiarioVistaMapper {

  private BeneficiarioVistaMapper() {}

  public static BeneficiarioTarjeta aTarjeta(BeneficiarioDetalleMsg msg) {
    List<NecesidadVistaMsg> activas = msg.getNecesidadesList().stream()
        .filter(n -> pendiente(n) > 0)
        .toList();

    String urgente = activas.stream()
        .max(Comparator.comparingDouble(BeneficiarioVistaMapper::proporcionPendiente)
            .thenComparingDouble(BeneficiarioVistaMapper::pendiente))
        .map(n -> n.getDescripcion() + " · " + numero(pendiente(n)) + " " + unidad(n.getUnidadMedida()))
        .orElse(null);

    return BeneficiarioTarjeta.builder()
        .id(msg.getId())
        .razonSocial(msg.getRazonSocial())
        .iniciales(iniciales(msg.getRazonSocial()))
        .direccion(msg.getDireccion())
        .necesidadUrgente(urgente)
        .necesidadesActivas(activas.size())
        .build();
  }

  public static NecesidadFila aFila(NecesidadVistaMsg n) {
    double requerida = cantidad(n.getCantidadRequerida());
    double recibida = cantidad(n.getCantidadRecibida());
    int porcentaje = requerida <= 0 ? 100 : (int) Math.min(100, Math.round(recibida * 100 / requerida));

    return NecesidadFila.builder()
        .descripcion(n.getDescripcion())
        .categoria(DonacionVistaMapper.capitalizar(n.getSubcategoria().getCategoria().getNombre()))
        .subcategoria(DonacionVistaMapper.capitalizar(n.getSubcategoria().getNombre()))
        .tipo(tipo(n))
        .requerida(numero(requerida))
        .recibida(numero(recibida))
        .unidad(unidad(n.getUnidadMedida()))
        .porcentaje(porcentaje)
        .cubierta(porcentaje >= 100)
        .build();
  }

  public static String iniciales(String razonSocial) {
    List<String> palabras = Arrays.stream(razonSocial.trim().split("\\s+"))
        .filter(p -> !p.isEmpty())
        .toList();
    if (palabras.isEmpty()) return "?";
    if (palabras.size() == 1) return palabras.get(0).substring(0, Math.min(2, palabras.get(0).length())).toUpperCase();
    List<String> significativas = palabras.size() > 2 ? palabras.subList(palabras.size() - 2, palabras.size()) : palabras;
    return significativas.stream()
        .map(p -> p.substring(0, 1).toUpperCase())
        .reduce("", String::concat);
  }

  private static String tipo(NecesidadVistaMsg n) {
    if ("RECURRENTE".equals(n.getTipo())) {
      return n.getFrecuencia().isBlank() ? "Recurrente" : "Recurrente · " + n.getFrecuencia().toLowerCase();
    }
    return "Extraordinaria";
  }

  private static double pendiente(NecesidadVistaMsg n) {
    return Math.max(0, cantidad(n.getCantidadRequerida()) - cantidad(n.getCantidadRecibida()));
  }

  private static double proporcionPendiente(NecesidadVistaMsg n) {
    double requerida = cantidad(n.getCantidadRequerida());
    return requerida <= 0 ? 0 : pendiente(n) / requerida;
  }

  private static double cantidad(String valor) {
    try {
      return Double.parseDouble(valor);
    } catch (NumberFormatException | NullPointerException e) {
      return 0;
    }
  }

  private static String numero(double valor) {
    return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
  }

  private static String unidad(String unidadMedida) {
    return switch (unidadMedida) {
      case "UNIDADES" -> "u.";
      case "KILOGRAMOS" -> "kg";
      case "GRAMOS" -> "g";
      case "LITROS" -> "l";
      case "MILILITROS" -> "ml";
      default -> unidadMedida.toLowerCase();
    };
  }
}
