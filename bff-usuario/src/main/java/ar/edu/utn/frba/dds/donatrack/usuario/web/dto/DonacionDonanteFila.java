package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DonacionDonanteFila {
  private final String codigo;
  private final String descripcion;
  private final String categoria;
  private final String subcategoria;
  private final String estadoClave;
  private final String estadoTexto;
}
