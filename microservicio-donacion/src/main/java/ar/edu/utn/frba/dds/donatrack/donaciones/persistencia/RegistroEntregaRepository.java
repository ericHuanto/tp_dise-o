package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.Optional;

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

  public Optional<RegistroEntrega> buscarUltimoPorDonante(Long idDonante) {
    return entityManager()
        .createQuery("SELECT r FROM RegistroEntrega r WHERE r.donante.id = :id "
            + "ORDER BY r.fecha DESC, r.id DESC", RegistroEntrega.class)
        .setParameter("id", idDonante)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }
}
