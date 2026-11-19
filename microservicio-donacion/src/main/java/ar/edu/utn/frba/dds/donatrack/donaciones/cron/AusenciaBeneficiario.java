package ar.edu.utn.frba.dds.donatrack.donaciones.cron;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.App;
import java.util.List;

public class AusenciaBeneficiario {
  public static void main(String[] args) {
    int codigoSalida = 0;
    try {
      App.configurarPersistencia();
      ejecutar();
      System.out.println("[Cron] Aviso a donantes ausentes finalizado.");
    } catch (Exception e) {
      System.err.println("[Cron] Error avisando a donantes ausentes: " + e.getMessage());
      e.printStackTrace();
      codigoSalida = 1;
    }
    System.exit(codigoSalida);
  }

  private static void ejecutar() {
    DonanteRepository repoDonantes = DonanteRepository.getInstancia();
    List<Donante> donantesAusentes = repoDonantes.buscarAusentesPorMas(20);
    donantesAusentes.forEach(d -> d.recibirNotificacion(
        "Vuelve a donar por favor. Eso ayuda a personas que lo necesitan"));
  }
}
