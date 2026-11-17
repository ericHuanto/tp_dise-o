package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.ruta.RutaResponse;
import java.util.List;

public class RutaMapper {

  public static RutaResponse aDto(Ruta ruta) {
    return new RutaResponse(
        ruta.getId(),
        ruta.getCamion().getPatente(),
        ruta.getChofer()==null ? null : ruta.getChofer().getNombre(),
        ruta.getFecha(),
        ruta.isEstaIniciada(),
        EntregaMapper.aDtoResumen(ruta.getEntregasOrdenadas())
    );
  }

  public static List<RutaResponse> aDto(List<Ruta> rutas) {
    return rutas.stream().map(RutaMapper::aDto).toList();
  }

}
