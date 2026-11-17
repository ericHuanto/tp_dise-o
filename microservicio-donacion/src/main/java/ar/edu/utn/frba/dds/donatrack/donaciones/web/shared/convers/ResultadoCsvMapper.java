package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ResultadoImportacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.resumenimportacion.ResultadoImportacionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultadoCsvMapper {

  public static ResultadoImportacionDto aDto(ResultadoImportacion resultado) {
    return new ResultadoImportacionDto(
        resultado.getDonantes().size(),
        resultado.getRegistroFallas()
    );
  }

}
