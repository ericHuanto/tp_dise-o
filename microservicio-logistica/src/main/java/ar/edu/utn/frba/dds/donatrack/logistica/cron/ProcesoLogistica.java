package ar.edu.utn.frba.dds.donatrack.logistica.cron;

import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.web.coordinadores.CoordinadorRuta;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonacionesApi;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.planificadorexterno.ClientePlanificadorExternoMock;

public class ProcesoLogistica {
  private final CoordinadorRuta coordinadorRuta;

  public ProcesoLogistica(CoordinadorRuta coordinadorRuta) {
    this.coordinadorRuta = coordinadorRuta;
  }

  public void ejecutar() {
    System.out.println("[ProcesoLogistica] Iniciando planificación diaria de rutas...");
    coordinadorRuta.ejecutarPlanificacionDiaria();
    System.out.println("[ProcesoLogistica] Planificación diaria enviada al componente externo.");
  }

  public static void main(String[] args) {
    CoordinadorRuta coordinadorRuta = new CoordinadorRuta(
        RutaRepository.getInstancia(),
        CamionRepository.getInstancia(),
        EntregaRepository.getInstancia(),
        BeneficiarioRepository.getInstancia(),
        new ConectorDonacionesApi(),
        new ClientePlanificadorExternoMock()
    );

    new ProcesoLogistica(coordinadorRuta).ejecutar();
  }
}