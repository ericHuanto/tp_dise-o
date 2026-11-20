package ar.edu.utn.frba.dds.donatrack.usuario.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CamionSeguimiento {
  private final String patente;
  private final String latitud;
  private final String longitud;
  private final String ultimaActualizacion;
  private final List<DetalleDonacion> detallesDonaciones;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class DetalleDonacion {
    private final String codigo;
    private final String descripcion;
    private final String destino;
    private final String ubicacion;
    private final String latitudDestino;
    private final String longitudDestino;
  }
}