package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BeneficiarioTarjeta {
  private final Long id;
  private final String razonSocial;
  private final String iniciales;
  private final String direccion;
  private final String necesidadUrgente;
  private final int necesidadesActivas;
}
