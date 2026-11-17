package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.NoPerecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.Perecedero;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import java.time.LocalDate;

public class BienFactory {
  private BienFactory() {}

  public static Bien crear(String tipo, String descripcion, Float cantidad, UnidadMedida unidad,
                           String foto, Subcategoria subcategoria,
                           LocalDate fechaVencimiento, Boolean usado) {
    if (tipo == null) throw new DominioException("El campo 'tipo' es obligatorio, en el Bien");
    return switch (tipo.toUpperCase()) {
      case "PERECEDERO" ->
          new Perecedero(descripcion, cantidad, unidad, foto, subcategoria, fechaVencimiento);
      case "NO_PERECEDERO" ->
          new NoPerecedero(descripcion, cantidad, unidad, foto, subcategoria, usado);
      default -> throw new DominioException("Tipo de bien desconocido: " + tipo);
    };
  }
}