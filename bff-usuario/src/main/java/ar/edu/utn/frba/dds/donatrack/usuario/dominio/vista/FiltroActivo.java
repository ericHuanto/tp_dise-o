package ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Chip de "Filtros activos": qué se está filtrando y a qué URL ir para quitarlo. */
@Getter
@AllArgsConstructor
public class FiltroActivo {
  private final String texto;
  private final String urlQuitar;
}
