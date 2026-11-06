package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RegistroNoEncontradoException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class DonacionRepository implements WithSimplePersistenceUnit {
  private static final DonacionRepository INSTANCE = new DonacionRepository();
  private DonacionRepository() { }
  public void guardar(Donacion donacion) {
    entityManager().persist(donacion);
  }

  public Donacion buscarPorId(Long id) {                 // era String
    return entityManager().find(Donacion.class, id);
  }
  public static DonacionRepository getInstancia() {
    return INSTANCE;
  }

  public List<Donacion> buscarPorEstadoActualCoincidenteCon(TipoEstadoDonacion estado) {
    return entityManager()
        .createQuery("SELECT d FROM Donacion d " +
            "JOIN d.historialEstados e " +
            "WHERE INDEX(e) = (SELECT MAX(INDEX(e2)) FROM Donacion d2 JOIN d2.historialEstados e2 WHERE d2 = d) " +
            "AND e.tipoEstado = :estado", Donacion.class)
        .setParameter("estado", estado)
        .getResultList();
  }

  public List<Donacion> buscarTodoPorEstado(TipoEstadoDonacion estado) {
    return buscarTodos().stream().filter(d -> d.getEstadoActual() == estado).toList();
  }

  public List<Donacion> buscarTodos() {
    return entityManager()
        .createQuery("SELECT d FROM Donacion d", Donacion.class)
        .getResultList();
  }

  public void actualizar(Donacion donacion) {
    if (donacion.getId() == null || buscarPorId(donacion.getId()) == null) {
      throw new RegistroNoEncontradoException("No se puede actualizar: no existe en la base de datos la donacion: " + donacion.getId());
    }
    entityManager().merge(donacion);
  }

  public void eliminar(Donacion donacion) {
   entityManager().remove(donacion);
  }

  public List<Donacion> buscarPorDonante(Donante donante, TipoEstadoDonacion estado,
                                         Long idCategoria, List<Long> idsSubcategorias) {
    return entityManager()
        .createQuery("SELECT d FROM Donacion d JOIN d.donantes dn WHERE dn.id = :idDonante", Donacion.class)
        .setParameter("idDonante", donante.getId())
        .getResultList()
        .stream()
        .filter(d -> estado == null || d.getEstadoActual() == estado)
        .filter(d -> idCategoria == null || d.getSubcategoria().getCategoria().getId().equals(idCategoria))
        .filter(d -> idsSubcategorias.isEmpty() || idsSubcategorias.contains(d.getSubcategoria().getId()))
        .toList();
  }

}
