package ar.edu.utn.frba.dds.donatrack.donacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.FilaError;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.GeneradorDonantesCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ImportadorCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.ImportadorCsvException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class GeneradorDonantesCsvTest {
  private String archivoExistente;
  private String archivoInexistente;

  @BeforeEach
  public void configuracionInicial() {
    archivoExistente = "src/test/resources/simple.csv"; //4 donantes 2 validos
    archivoInexistente = "no_existo.csv";
  }

  @Test
  public void GenerarDonantesDeUnArchivoSimple() {
    DonanteRepository repoDonantes = mock(DonanteRepository.class);
    ImportadorCsv importador = new ImportadorCsv();
    GeneradorDonantesCsv generadorDonantes = new GeneradorDonantesCsv(repoDonantes, importador);

    //assertEquals(0, repoDonantes.buscarTodos().size());
    //primera prueba verificamos que la lista este vacía

    List<FilaError> errores = generadorDonantes.iniciarCarga(archivoExistente);
    errores.forEach(e -> System.out.println(" fila " + e.getNumeroFila() + " motivo: " + e.getMotivoError()));

    //assertEquals(2, repoDonantes.buscarTodos().size());
    verify(repoDonantes, times(2)).guardar(any());
    assertEquals(2, errores.size());
  }

  @Test
  public void LectorLanzaExcepcionSiElArchivoNoExiste() {
    DonanteRepository repoDonantes = DonanteRepository.getInstancia();
    ImportadorCsv importador = new ImportadorCsv();
    GeneradorDonantesCsv generadorDonantes = new GeneradorDonantesCsv(repoDonantes, importador);

    assertThrows(ImportadorCsvException.class, () -> generadorDonantes.iniciarCarga(archivoInexistente));
  }

}
