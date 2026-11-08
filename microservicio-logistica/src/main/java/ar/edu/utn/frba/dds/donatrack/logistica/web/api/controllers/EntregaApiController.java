package ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers.EntregaMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.entrega.EntregaFotoRequest;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.entrega.EntregaNoRecibidaRequest;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonaciones;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BodyException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.UUID;

public class EntregaApiController {
  private final EntregaRepository repoEntregas;
  private final ConectorDonaciones donacionesBridge;

  public EntregaApiController(EntregaRepository repository, ConectorDonaciones donacionesClient) {
    this.repoEntregas = repository;
    this.donacionesBridge = donacionesClient;
  }

  public void obtenerTodos(Context ctx) {
    List<Entrega> entregas = repoEntregas.buscarTodas();

    ctx.status(200).json(EntregaMapper.aDtoResumen(entregas));
  }

  public void obtener(Context ctx) {
    Long id = Long.valueOf(ctx.pathParam("id"));

    Entrega entrega = buscarEntregaPorId(id);
    ctx.status(200).json(EntregaMapper.aDto(entrega));
  }

  public void agregarFoto(Context ctx) {
    //Cosas que recibo por URL
    Long id = Long.valueOf(ctx.pathParam("id"));
    //Cosas que recibo por Body
    EntregaFotoRequest request = ctx.bodyAsClass(EntregaFotoRequest.class);
    String urlFoto = request.urlFoto();

    Entrega entrega = buscarEntregaPorId(id);
    entrega.agregarFotoRecepcion(urlFoto);
    repoEntregas.actualizar(entrega);
    ctx.status(200);
  }

  public void confirmarRecibida(Context ctx) {
    Long id = Long.valueOf(ctx.pathParam("id"));

    Entrega entrega = buscarEntregaPorId(id);
    entrega.confirmarRecepcion();
    repoEntregas.actualizar(entrega);
    comunicarAlasDonacionesSuRecepcion(entrega.getDonaciones(), "https://..../comprobantes/...");
    ctx.status(200);
  }

  public void confirmarNoRecibida(Context ctx) {
    //Cosas que recibo por URL
    Long id = Long.valueOf(ctx.pathParam("id"));
    //Cosas que recibo por Body
    EntregaNoRecibidaRequest request = ctx.bodyAsClass(EntregaNoRecibidaRequest.class);
    if (request.motivo()==null) throw new BodyException("Bad Request, necesita: 'motivo' ");
    String motivo = request.motivo();

    Entrega entrega = buscarEntregaPorId(id);
    entrega.marcarNoRecibida(motivo);
    repoEntregas.actualizar(entrega);
    comunicarAlasDonacionesErrorRecepcion(entrega.getDonaciones(), motivo);
    ctx.status(200);
  }

  public void reingresarADeposito(Context ctx) {
    Long id = Long.valueOf(ctx.pathParam("id"));

    Entrega entrega = buscarEntregaPorId(id);
    entrega.reingresarDeposito();
    repoEntregas.eliminar(entrega.getId());
    comunicarAlasDonacionesReingresoAdeposito(entrega.getDonaciones());
    ctx.status(200);
  }
  public void mostrarMisEntregas(Context ctx) {
    List<Entrega> entregas = repoEntregas.buscarTodas();

    List<Map<String, Object>> datos = entregas.stream()
        .map(entrega -> {
          Map<String, Object> item = new HashMap<>();
          item.put("id", entrega.getId());
          item.put("estado", entrega.getEstadoActual().name());
          item.put("puedeConfirmar",
              entrega.getEstadoActual() == TipoEstadoEntrega.EN_TRASLADO);
          return item;
        })
        .toList();

    ctx.render("mis_entregas.hbs", Map.of("entregas", datos));
  }

  public void mostrarFormularioConfirmacion(Context ctx) {
    Long id = Long.valueOf(ctx.pathParam("id"));

    Entrega entrega = repoEntregas.buscarPorId(id);

    if (entrega == null) {
      ctx.status(404).result("Entrega no encontrada");
      return;
    }

    if (entrega.getEstadoActual() != TipoEstadoEntrega.EN_TRASLADO) {
      ctx.status(400).result("La entrega no está en traslado");
      return;
    }

    Map<String, Object> datosEntrega = Map.of(
        "id", entrega.getId(),
        "estado", entrega.getEstadoActual().name()
    );

    ctx.render("confirmar_recepcion.hbs",
        Map.of("entrega", datosEntrega));
  }

  public void confirmarRecepcionDesdeVista(Context ctx) throws IOException {
    Long id = Long.valueOf(ctx.pathParam("id"));

    Entrega entrega = buscarEntregaPorId(id);

    if (entrega.getEstadoActual() != TipoEstadoEntrega.EN_TRASLADO) {
      ctx.status(400).result("La entrega no está en traslado");
      return;
    }

    List<UploadedFile> fotos = ctx.uploadedFiles("fotos");

    if (fotos.isEmpty()) {
      ctx.status(400).result("Debe adjuntar al menos una fotografía");
      return;
    }

    Path directorio = Path.of("microservicio-logistica", "uploads", "entregas");
    Files.createDirectories(directorio);

    List<String> urlsFotos = new ArrayList<>();

    for (UploadedFile foto : fotos) {
      String tipo = foto.contentType();

      if (tipo == null || !List.of(
          "image/jpeg", "image/png", "image/webp"
      ).contains(tipo)) {
        ctx.status(400).result("Solo se permiten imágenes JPG, PNG o WebP");
        return;
      }
    }

    for (UploadedFile foto : fotos) {
      String extension = switch (foto.contentType()) {
        case "image/png" -> ".png";
        case "image/webp" -> ".webp";
        default -> ".jpg";
      };

      String nombre = UUID.randomUUID() + extension;
      Path destino = directorio.resolve(nombre);

      try (var entrada = foto.content()) {
        Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
      }

      urlsFotos.add("/uploads/entregas/" + nombre);
    }

    entrega.confirmarRecepcion();

    for (String url : urlsFotos) {
      entrega.agregarFotoRecepcion(url);
    }

    repoEntregas.actualizar(entrega);

    ctx.redirect("/mis-entregas");
  }

  //================== FUNCIONES AUXILIARES =====================
  private Entrega buscarEntregaPorId(Long id) {
    Entrega entrega = repoEntregas.buscarPorId(id);
    if (entrega == null) throw new RecursoNoEncontradoException("Entrega no encontrada: " + id);

    return entrega;
  }

  private void comunicarAlasDonacionesSuRecepcion(List<DonacionEnTransito> donaciones, String linkComprobante) {
    donaciones.forEach(d ->
        donacionesBridge.marcarDonacionEntregaExitosa(d.getId(), linkComprobante)
    );
  }

  private void comunicarAlasDonacionesErrorRecepcion(List<DonacionEnTransito> donaciones, String motivo) {
    donaciones.forEach(d ->
        donacionesBridge.marcarDonacionErrorEntrega(d.getId(), motivo)
    );
  }

  private void comunicarAlasDonacionesReingresoAdeposito(List<DonacionEnTransito> donaciones) {
    donaciones.forEach(d ->
        donacionesBridge.marcarDonacionVueltaDeposito(d.getId())
    );
  }

}