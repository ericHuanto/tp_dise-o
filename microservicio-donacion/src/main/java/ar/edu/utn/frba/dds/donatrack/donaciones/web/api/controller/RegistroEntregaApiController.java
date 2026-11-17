package ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.BienFactory;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.segmentador.SegmentadorDonaciones;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.RegistroEntregaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.registro.BienRequest;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.registro.RegistroEntregaRequest;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistroEntregaApiController implements WithSimplePersistenceUnit {
  private final RegistroEntregaRepository repoRegistros;
  private final DonacionRepository repoDonaciones;
  private final DonanteRepository repoDonantes;
  private final SubcategoriaRepository repoSubcategorias;
  private final SegmentadorDonaciones segmentador = new SegmentadorDonaciones();

  public RegistroEntregaApiController(RegistroEntregaRepository registroRepository,
                                      DonacionRepository donacionRepository,
                                      DonanteRepository donanteRepository,
                                      SubcategoriaRepository subcategoriaRepository) {
    this.repoRegistros = registroRepository;
    this.repoDonaciones = donacionRepository;
    this.repoDonantes = donanteRepository;
    this.repoSubcategorias = subcategoriaRepository;
  }

  public void crear(Context context) {
    RegistroEntregaRequest request = context.bodyAsClass(RegistroEntregaRequest.class);
    validar(request);

    Donante donante = repoDonantes.buscarPorId(request.donanteId());
    if (donante == null) throw new DominioException("La persona donante no existe");

    List<Bien> bienes = request.bienes().stream().map(this::aBien).toList();
    RegistroEntrega registro =
        new RegistroEntrega(request.fechaRecepcion(), request.observaciones(), bienes, donante);
    List<Donacion> donaciones = segmentador.segmentar(List.of(registro));

    beginTransaction();
    try {
      donaciones.forEach(repoDonaciones::guardar);
      repoRegistros.guardar(registro);
      commitTransaction();
    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }

    context.status(201).json(Map.of(
        "id", registro.getId(),
        "cantidad_donaciones", donaciones.size()));
  }

  //============= FUNCIONES AUXILIARES =====================
  private void validar(RegistroEntregaRequest request) {
    if (request == null || request.donanteId() == null)
      throw new DominioException("Elegí una persona donante");
    if (request.bienes() == null || request.bienes().isEmpty())
      throw new DominioException("La entrega debe tener al menos un bien");
    request.bienes().forEach(b -> {
      if (b.descripcion() == null || b.descripcion().isBlank())
        throw new DominioException("Todos los bienes necesitan descripción");
    });
  }

  private Bien aBien(BienRequest b) {
    Subcategoria subcategoria = repoSubcategorias.buscarPorNombre(b.categoria(), b.subcategoria());
    if (subcategoria == null)
      throw new DominioException("No existe la subcategoría '" + b.subcategoria()
          + "' en la categoría '" + b.categoria() + "'");

    return BienFactory.crear(b.tipo(), b.descripcion().trim(), b.cantidad(),
        aUnidadMedida(b.unidadMedida()), null, subcategoria, b.fechaVencimiento(), b.usado());
  }

  private UnidadMedida aUnidadMedida(String unidad) {
    if (unidad == null) throw new DominioException("El campo 'unidad_medida' es obligatorio, en el Bien");
    try {
      return UnidadMedida.valueOf(unidad.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DominioException("La unidad de medida: " + unidad + " no existe, debe ser: "
          + Arrays.toString(UnidadMedida.values()));
    }
  }
}