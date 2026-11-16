package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import com.google.protobuf.ByteString;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CsvController implements WithSimplePersistenceUnit {
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;
  private final UsuarioRepository repoUsuarios;

  private static final ConcurrentHashMap<String, Map<String, Object>> tareasEnProceso = new ConcurrentHashMap<>();

  public CsvController(DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub, UsuarioRepository repoUsuarios) {
    this.donacionesStub = donacionesStub;
    this.repoUsuarios = repoUsuarios;
  }

  public void mostrarFormularioCreacion(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    context.render("donaciones/views/admin/importar_donantes.hbs", modelo);
  }

  public void procesarDonantesAntiguos(Context context) {
    UploadedFile archivoCargadoCsv = context.uploadedFile("archivo_csv");
    checkQueSubaArchivo(archivoCargadoCsv);
    checkQueSeaArchivoValido(archivoCargadoCsv);

    byte[] bytesArchivo;
    String nombreOriginal = archivoCargadoCsv.filename();
    try {
      bytesArchivo = archivoCargadoCsv.content().readAllBytes();
    } catch (Exception e) {
      throw new RuntimeException("Error al leer el archivo CSV subido", e);
    }

    String idTarea = UUID.randomUUID().toString().substring(0, 8);
    context.sessionAttribute("tarea_csv_activa", idTarea);
    tareasEnProceso.put(idTarea, Map.of("cantExitosa", -1, "erroresEncontrados", List.of()));

    CompletableFuture.runAsync(() -> {
      try {
        //Enviamos el archivo por gRPC a microservicio-donacion para que cree los Donantes
        ImportarCsvResponse response = donacionesStub.importarDonantesCsv(
            ImportarCsvRequest.newBuilder()
                .setNombreArchivo(nombreOriginal)
                .setContenidoCsv(ByteString.copyFrom(bytesArchivo))
                .build()
        );

        //Guardamos los Usuarios en la DB del BFF
        beginTransaction();
        for (DonanteImportadoUsuarioMsg uMsg : response.getUsuariosGeneradosList()) {
          Usuario nuevoUsuario = new Usuario(uMsg.getUsername(), uMsg.getPassword());
          nuevoUsuario.vincularDonante(uMsg.getIdDonante());
          repoUsuarios.guardar(nuevoUsuario);
        }
        commitTransaction();

        tareasEnProceso.put(idTarea, Map.of(
            "cantExitosa", response.getCantExitosa(),
            "erroresEncontrados", response.getErroresEncontradosList()
        ));
      } catch (Exception e) {
        if (entityManager().getTransaction().isActive()) {
          rollbackTransaction();
        }
        List<FilaErrorMsg> errorCritico = List.of(
            FilaErrorMsg.newBuilder()
                .setNumeroFila(0)
                .setMotivoError("Error crítico interno: " + e.getMessage())
                .build()
        );
        tareasEnProceso.put(idTarea, Map.of(
            "cantExitosa", 0,
            "erroresEncontrados", errorCritico
        ));
      } finally {
        WithSimplePersistenceUnit.dispose();
      }
    });

    context.redirect("/donantes/importacion/estado/" + idTarea);
  }

  public void mostrarPantallaEspera(Context context) {
    String idTarea = context.pathParam("id_tarea");
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("idTarea", idTarea);
    context.render("donaciones/views/admin/importar_donantes_espera.hbs", modelo);
  }

  public void consultarEstadoProceso(Context context) {
    String idTarea = context.pathParam("id_tarea");
    Map<String, Object> resultado = tareasEnProceso.get(idTarea);

    if (resultado == null) {
      context.status(404).json(Map.of("estado", "NO_ENCONTRADO"));
    } else if ((int) resultado.get("cantExitosa") == -1) {
      context.json(Map.of("estado", "PROCESANDO"));
    } else {
      context.json(Map.of("estado", "COMPLETADO"));
    }
  }

  public void mostrarResultados(Context context) {
    String idTarea = context.pathParam("id_tarea");
    Map<String, Object> resultado = tareasEnProceso.remove(idTarea);
    context.sessionAttribute("tarea_csv_activa", null);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("resultado", resultado);

    context.render("donaciones/views/admin/importar_donantes_resultado.hbs", modelo);
  }

  //================== FUNCIONES AUXILIARES ====================
  //Un usuario común no va a recibir estas excepciones, pero uno que trata de hacer cosas raras sii.
  private void checkQueSubaArchivo(UploadedFile archivoCargado) {
    if (archivoCargado == null) {
      throw new RuntimeException("No se detectó ningún archivo en la petición.");
    }
  }

  private void checkQueSeaArchivoValido(UploadedFile archivoCsv) {
    if (archivoCsv.contentType() == null || !archivoCsv.contentType().equals("text/csv")) {
      throw new RuntimeException("El archivo '" + archivoCsv.filename() + "' no es válido, Solo se permite archivo CSV");
    }

    //if (!archivoCsv.filename().toLowerCase().endsWith(".csv")) {
    //  throw new RuntimeException("El archivo '" + archivoCsv.filename() + "' no es válido. Solo se permiten archivos .csv");
    //}
  }
}