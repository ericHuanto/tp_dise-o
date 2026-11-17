package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.gps.GpsDto;

import java.util.List;

public class GpsMapper {

  public static GpsDto aDto(Gps gps) {
    return new GpsDto(
        gps.getImei(),
        gps.getNivelBateria()
    );
  }

  public static List<GpsDto> aDto(List<Gps> listaGps) {
    return listaGps.stream().map(g -> aDto(g)).toList();
  }

}
