package ar.edu.utn.frba.dds.donatrack.logistica.persistencia;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BaseDatoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;

import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RegistroNoEncontradoException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.*;

public class RutaRepository implements WithLogisticaPersistenceUnit {
  private static final RutaRepository INSTANCE = new RutaRepository();

  private RutaRepository() { }

  public static RutaRepository getInstancia() {
    return INSTANCE;
  }

  public void guardar(Ruta ruta) {
    if (ruta.getId() != null) {
      throw new BaseDatoException("Constraint Violations: La ruta ya tiene un ID asignado: " + ruta.getId());
    }
    entityManager().persist(ruta);
  }

  public Ruta buscarPorId(Long id) {
    return entityManager().find(Ruta.class, id);
  }

  public List<Ruta> buscarTodas() {
    return entityManager().createQuery("SELECT r FROM Ruta r", Ruta.class)
        .getResultList();
  }

  public void actualizar(Ruta ruta) {
    if (ruta.getId() == null || buscarPorId(ruta.getId()) == null) {
      throw new RegistroNoEncontradoException("No se puede actualizar: no existe en la base de datos la ruta: " + ruta.getId());
    }
    entityManager().merge(ruta);
  }

  public void eliminar(Long id) {
    Ruta ruta = buscarPorId(id);
    if (ruta == null) {
      throw new RegistroNoEncontradoException("No existe ruta con id " + id);
    }
    entityManager().remove(ruta);
  }

  public long camionesEnRutaAhora() {
    return entityManager()
        .createQuery("SELECT COUNT(DISTINCT r.camion) " +
                "FROM Ruta r " +
                "WHERE r.estaIniciada = true", Long.class)
        .getSingleResult();
  }

}