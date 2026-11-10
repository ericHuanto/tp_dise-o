package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.FilaError;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.GeneradorDonantesCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ImportadorCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CsvController {
  private final Path directorioCsv = Path.of("microservicio-donacion", "uploads", "ex-donantes");
  private GeneradorDonantesCsv generadorDonantesCsv;

  public CsvController(DonanteRepository donanteRepository) {
    this.generadorDonantesCsv = new GeneradorDonantesCsv(donanteRepository, new ImportadorCsv());
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

    context.render("importar_donantes.hbs", modelo);
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

    List<FilaError> errores = generadorDonantesCsv.iniciarCarga(rutaDestino.toString());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("errores", errores);
    modelo.put("mensaje", "Archivo guardado e importado con éxito.");

    context.render("importar_donantes_resultado.hbs", modelo);
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

}
