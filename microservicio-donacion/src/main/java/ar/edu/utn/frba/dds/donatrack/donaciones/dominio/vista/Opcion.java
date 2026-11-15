package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.vista;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Opcion {
  private final String valor;
  private final String texto;
  private final boolean seleccionado;

  public Opcion(String valor, String texto) {
    this(valor, texto, false);
  }
}
