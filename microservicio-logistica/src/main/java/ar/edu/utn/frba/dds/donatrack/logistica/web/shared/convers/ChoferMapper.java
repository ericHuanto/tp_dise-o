package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Chofer;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.chofer.ChoferDto;

public class ChoferMapper {

  public static Chofer aDominio(ChoferDto choferDto) {
    return new Chofer(
        choferDto.nombre(),
        choferDto.apellido(),
        choferDto.licenciaConducir()
    );
  }

}
