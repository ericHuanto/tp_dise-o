package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.resumenimportacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.FilaError;

import java.util.List;

public record ResultadoImportacionDto(
  int cantExitosa,
  List<FilaError> erroresEncontrados
) { }
