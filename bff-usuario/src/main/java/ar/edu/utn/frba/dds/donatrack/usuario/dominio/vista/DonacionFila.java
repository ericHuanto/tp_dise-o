package ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DonacionFila {
  private final Long id;
  private final String codigo;
  private final List<String> donantes;
  private final String fechaIngreso;
  private final List<BienFila> bienes;
  private final String vencimiento;        // null si no es perecedero
  private final String vencimientoTexto;
  private final String vencimientoNivel;   // vencido | pronto | normal
  private final String estadoClave;
  private final String estadoTexto;
  private final Boolean vencible;

  @Getter
  @Builder
  public static class BienFila {
    private final String descripcion;
    private final String categoria;
    private final String subcategoria;
    private final String cantidad;
    private final String unidadMedida;
  }
}