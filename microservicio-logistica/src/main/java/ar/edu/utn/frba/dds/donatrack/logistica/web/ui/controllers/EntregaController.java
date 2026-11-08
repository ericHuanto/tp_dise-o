package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers.EntregaMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.entrega.EntregaResumenResponse;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class EntregaController implements WithLogisticaPersistenceUnit {
  private final Path directorioFotos = Path.of("microservicio-logistica", "uploads", "entregas");
  private EntregaRepository repoEntregas;

  public EntregaController(EntregaRepository entregaRepository) {
    this.repoEntregas = entregaRepository;
    try {
      if (!Files.exists(directorioFotos)) {
        Files.createDirectories(directorioFotos);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error fatal: No se pudo crear el directorio para las fotos de entregas en " + directorioFotos.toAbsolutePath(), e);
    }
  }

  public void mostrarMisEntregas(Context context) {
    List<Entrega> entregas = repoEntregas.buscarPorEstadoActual(TipoEstadoEntrega.EN_TRASLADO);
    System.err.println("cant de entregas: " + entregas.size());
    List<EntregaResumenResponse> entregasDto = EntregaMapper.aDtoResumen(entregas);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("entregas", entregasDto);
    //modelo.put("usuario", context.sessionAttribute("usuario_actual"));

    context.render("mis_entregas.hbs", modelo);
  }

  public void mostrarFormularioConfirmacion(Context context) {
    Long id = Long.valueOf(context.pathParam("id"));
    Entrega entrega = buscarEntregaPorId(id);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("entrega", EntregaMapper.aDto(entrega)); //NO vamos a usar el dtoResumen porque vamos a mostrar mas informacion //TODO acutalizar el .hbs para que muestre mas datos

    context.render("confirmar_recepcion.hbs", modelo);
  }

  public void confirmarRecepcion(Context context) {
    Long id = Long.valueOf(context.pathParam("id"));
    Entrega entrega = buscarEntregaPorId(id);

    List<UploadedFile> fotos = context.uploadedFiles("fotos");
    checkQueTengaFotos(fotos);
    checkQueSeaFotosValida(fotos);

    entrega.confirmarRecepcion();

    try {
      for (UploadedFile foto : fotos) {
        String nombre = UUID.randomUUID().toString().substring(0, 8) + "-" + foto.filename();
        Path rutaDestino = directorioFotos.resolve(nombre);
        Files.copy(foto.content(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
        String url = "/uploads/entregas/" + nombre;
        entrega.agregarFotoRecepcion(url);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error de conexión con el disco rígido", e);
    }

    beginTransaction();
    repoEntregas.actualizar(entrega);
    commitTransaction();

    context.redirect("/mis-entregas");
  }

  //=============== FUNCIONES AUXILIARES ===================
  //Un usuario común no va a recibir estas excepciones, pero uno que trata de hacer cosas raras sii.
  private Entrega buscarEntregaPorId(Long id) {
    Entrega entrega = repoEntregas.buscarPorId(id);
    if (entrega == null) throw new RuntimeException("Entrega no encontrada: " + id);
    return entrega;
  }

  private void checkQueTengaFotos(List<UploadedFile> fotos) {
    if (fotos == null || fotos.isEmpty()) {
      throw new RuntimeException("Error: Debe adjuntar al menos una fotografía de la recepción.");
    }
  }

  private void checkQueSeaFotosValida(List<UploadedFile> fotos) {
    List<String> tipoImagenesPermitidas = List.of("image/jpeg", "image/png", "image/webp");

    for (UploadedFile foto : fotos) {
      if (!tipoImagenesPermitidas.contains(foto.contentType())) {
        throw new RuntimeException("El archivo '" + foto.filename() + "' no es válido. Solo se permiten imágenes del tipo: " + tipoImagenesPermitidas);
      }
    }
  }

}
