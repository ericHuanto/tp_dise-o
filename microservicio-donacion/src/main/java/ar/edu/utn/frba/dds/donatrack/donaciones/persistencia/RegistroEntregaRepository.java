package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class RegistroEntregaRepository implements WithSimplePersistenceUnit {
  private static final RegistroEntregaRepository INSTANCE = new RegistroEntregaRepository();

  private RegistroEntregaRepository() { }

  public static RegistroEntregaRepository getInstancia() {
    return INSTANCE;
  }

  public void guardar(RegistroEntrega registro) {
    entityManager().persist(registro);
  }

  public RegistroEntrega buscarPorId(Long id) {
    return entityManager().find(RegistroEntrega.class, id);
  }
}
