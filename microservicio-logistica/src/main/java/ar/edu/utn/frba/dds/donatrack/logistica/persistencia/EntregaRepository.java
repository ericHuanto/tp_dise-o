package ar.edu.utn.frba.dds.donatrack.logistica.persistencia;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BaseDatoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RegistroNoEncontradoException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.*;

public class EntregaRepository implements WithLogisticaPersistenceUnit {
  private static final EntregaRepository INSTANCE = new EntregaRepository();

  private EntregaRepository() { }

  public static EntregaRepository getInstancia() {
    return INSTANCE;
  }

  public void guardar(Entrega entrega) {
    entityManager().persist(entrega);
  }

  public Entrega buscarPorId(Long id) {
    return entityManager().find(Entrega.class, id);
  }

  public List<Entrega> buscarPorEstadoActual(TipoEstadoEntrega estadoBuscado) {
    return entityManager()
        .createQuery("SELECT e FROM Entrega e " +
            "JOIN e.historialEstados h " +
            "WHERE INDEX(h) = (SELECT MAX(INDEX(h2)) FROM Entrega e2 JOIN e2.historialEstados h2 WHERE e2 = e) " +
            "AND h.tipoEstado = :estado", Entrega.class)
        .setParameter("estado", estadoBuscado)
        .getResultList();
  }

  public List<Entrega> buscarTodas() {
    return entityManager()
        .createQuery("SELECT e FROM Entrega e", Entrega.class)
        .getResultList();
  }

  public void actualizar(Entrega entrega) {
    if (entrega.getId() == null || buscarPorId(entrega.getId()) == null) {
      throw new RegistroNoEncontradoException(
          "No se puede actualizar: no existe en la base de datos la entrega: " + entrega.getId());
    }
    entityManager().merge(entrega);
  }

  public void eliminar(Long id) {
    Entrega entrega = buscarPorId(id);
    if (entrega == null) {
      throw new RegistroNoEncontradoException("No existe entrega con id " + id);
    }
    entityManager().remove(entrega);
  }
}