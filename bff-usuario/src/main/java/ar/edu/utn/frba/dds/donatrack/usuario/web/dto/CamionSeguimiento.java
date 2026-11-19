package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CamionSeguimiento {
  private final String patente;
  private final String latitud;
  private final String longitud;
  private final String ultimaActualizacion;
  private final List<String> donaciones;
}
