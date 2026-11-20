package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FiltroActivo {
  private final String texto;
  private final String urlQuitar;
}
