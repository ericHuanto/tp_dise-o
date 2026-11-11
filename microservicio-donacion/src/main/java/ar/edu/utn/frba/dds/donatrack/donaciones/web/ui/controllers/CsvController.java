package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.FilaError;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.GeneradorDonantesCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ImportadorCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ResultadoImportacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.ResultadoCsvMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.resumenimportacion.ResultadoImportacionDto;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CsvController implements WithSimplePersistenceUnit {
  private final Path directorioCsv = Path.of("microservicio-donacion", "uploads", "ex-donantes");
  private ImportadorCsv importador;
  private DonanteRepository repoDonantes;
  private UsuarioRepository repoUsuarios;
  private static final ConcurrentHashMap<String, ResultadoImportacionDto> tareasEnProceso = new ConcurrentHashMap<>();

  public CsvController(DonanteRepository donanteRepository, UsuarioRepository usuarioRepository) {
    this.importador = new ImportadorCsv();
    this.repoDonantes = donanteRepository;
    this.repoUsuarios = usuarioRepository;
    try {
      if (!Files.exists(directorioCsv)) {
        Files.createDirectories(directorioCsv);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error fatal: No se pudo crear la carpeta 'ex-donantes' para la carga de csv " + directorioCsv.toAbsolutePath(), e);
    }
  }

  public void mostrarFormularioCreacion(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    //modelo.put("usuario", context.sessionAttribute("usuario_actual")); de aca vamos a ver si tiene permiso para verlo

    context.render("views/admin/importar_donantes.hbs", modelo);
  }

  public void procesarDonantesAntiguos(Context context) {
    UploadedFile archivoCargadoCsv = context.uploadedFile("archivo_csv");
    checkQueSubaArchivo(archivoCargadoCsv);
    checkQueSeaArchivoValido(archivoCargadoCsv);

    String nombreArchivo = UUID.randomUUID().toString().substring(0, 8) + "-" + archivoCargadoCsv.filename();
    Path rutaDestino = directorioCsv.resolve(nombreArchivo);

    try {
      Files.copy(archivoCargadoCsv.content(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Error de conexión con el disco rígido", e);
    }

    String idTarea = UUID.randomUUID().toString().substring(0, 8);
    context.sessionAttribute("tarea_csv_activa", idTarea);
    ResultadoImportacionDto TAREA_EN_PROCESO = new ResultadoImportacionDto(-1, new ArrayList<>());
    tareasEnProceso.put(idTarea, TAREA_EN_PROCESO);

    CompletableFuture.runAsync(() -> {
      try {
        ResultadoImportacion resultado = importador.importarDesdeArchivo(rutaDestino.toString());
        beginTransaction();
        for (Donante donante : resultado.getDonantes()) {
          String username = donante.getNombreCompleto().substring(0, 3) + donante.getDocumento().getDetalle();
          String password = generarContrasenia();
          Usuario nuevoUsuario = new Usuario(username, password);
          donante.vincularUsuario(nuevoUsuario);

          repoUsuarios.guardar(nuevoUsuario);
          repoDonantes.guardar(donante);

          donante.recibirNotificacion(String.format("Bienvenido al nuevo sistema de DonaTrack %s. Tu cuenta ha sido creada. \n Usuario: %s \n Contraseña temporal: %s \n ", donante.getNombreCompleto(), username, password));
        }
        commitTransaction();
        tareasEnProceso.put(idTarea, ResultadoCsvMapper.aDto(resultado));
      } catch (Exception e) {
        rollbackTransaction();
        List<FilaError> errorCritico = List.of(new FilaError(0, "Error crítico interno: " + e.getMessage()));
        tareasEnProceso.put(idTarea, new ResultadoImportacionDto(0, errorCritico));
      }
    });

    context.redirect("/donantes/importacion/estado/" + idTarea);
  }

  public void mostrarPantallaEspera(Context context) {
    String idTarea = context.pathParam("id_tarea");
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("idTarea", idTarea);
    context.render("views/admin/importar_donantes_espera.hbs", modelo);
  }

  //(API): Endpoint que JavaScript consulta cada 2 segundos
  public void consultarEstadoProceso(Context context) {
    String idTarea = context.pathParam("id_tarea");
    ResultadoImportacionDto resultado = tareasEnProceso.get(idTarea);

    if (resultado == null) {
      context.status(404).json(Map.of("estado", "NO_ENCONTRADO"));
    } else if (resultado.cantExitosa() == -1) {
      context.json(Map.of("estado", "PROCESANDO"));
    } else {
      context.json(Map.of("estado", "COMPLETADO"));
    }
  }

  public void mostrarResultados(Context context) {
    String idTarea = context.pathParam("id_tarea");
    ResultadoImportacionDto resultado = tareasEnProceso.remove(idTarea);
    context.sessionAttribute("tarea_csv_activa", null);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("resultado", resultado);

    context.render("views/admin/importar_donantes_resultado.hbs", modelo);
  }

  //================== FUNCIONES AUXILIARES ====================
  //Un usuario común no va a recibir estas excepciones, pero uno que trata de hacer cosas raras sii.
  private void checkQueSubaArchivo(UploadedFile archivoCargado) {
    if (archivoCargado == null) {
      throw new RuntimeException("No se detectó ningún archivo en la petición.");
    }
  }

  private void checkQueSeaArchivoValido(UploadedFile archivoCsv) {
    if (!archivoCsv.contentType().equals("text/csv")) {
      throw new RuntimeException("El archivo '" + archivoCsv.filename() + "' no es válido, Solo se permite archivo CSV");
    }

    //if (!archivoCsv.filename().toLowerCase().endsWith(".csv")) {
    //  throw new RuntimeException("El archivo '" + archivoCsv.filename() + "' no es válido. Solo se permiten archivos .csv");
    //}
  }

  private String generarContrasenia() {
    String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder codigo = new StringBuilder();

    for (int i = 0; i < 8; i++) {
      codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
    }

    return codigo.toString();
  }

}
