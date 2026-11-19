package ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NecesidadFila {
  private final String descripcion;
  private final String categoria;
  private final String subcategoria;
  private final String tipo;
  private final String requerida;
  private final String recibida;
  private final String unidad;
  private final int porcentaje;
  private final boolean cubierta;
}
