package ar.edu.utn.frba.dds.donatrack.donaciones.cron;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.GeneradorRankings;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.algoritmos.PrioridadSubAtendidos;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.RankingRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.App;
import java.util.List;

public class ProcesoMatchmaking {
  public static void main(String[] args) {
    int codigoSalida = 0;
    try {
      App.configurarPersistencia();
      ejecutar();
      System.out.println("[Cron] Matchmaking finalizado.");
    } catch (Exception e) {
      System.err.println("[Cron] Error en matchmaking: " + e.getMessage());
      e.printStackTrace();
      codigoSalida = 1;
    }
    System.exit(codigoSalida);
  }

  private static void ejecutar() {
    BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
    DonacionRepository repoDonaciones = DonacionRepository.getInstancia();
    RankingRepository repoRankings = RankingRepository.getInstancia();

    repoRankings.withTransaction(repoRankings::vaciarSoft);

    List<Beneficiario> beneficiarios = repoBeneficiarios.buscarTodos();
    List<Donacion> donaciones = repoDonaciones.buscarTodoPorEstado(TipoEstadoDonacion.EN_DEPOSITO);

    GeneradorRankings generadorRankings = new GeneradorRankings(repoRankings);
    generadorRankings.agregarAlgoritmo(new CompatibilidadSemantica());
    generadorRankings.agregarAlgoritmo(new PrioridadSubAtendidos());
    generadorRankings.generar(donaciones, beneficiarios);
  }
}
