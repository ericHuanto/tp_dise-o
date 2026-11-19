package ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista;

import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class ResumenDonaciones {
  private static final Set<String> EN_CURSO = Set.of("ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR", "EN_TRASLADO");

  private final int total;
  private final int entregadas;
  private final int enCurso;
  private final int pendientes;

  public ResumenDonaciones(List<String> estados) {
    this.total = estados.size();
    this.entregadas = (int) estados.stream().filter("ENTREGADA"::equals).count();
    this.enCurso = (int) estados.stream().filter(EN_CURSO::contains).count();
    this.pendientes = (int) estados.stream().filter("EN_DEPOSITO"::equals).count();
  }
}
