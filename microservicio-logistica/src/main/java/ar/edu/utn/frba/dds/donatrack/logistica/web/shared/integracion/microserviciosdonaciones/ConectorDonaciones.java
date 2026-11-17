package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import java.util.List;

public interface ConectorDonaciones {
  List<DonacionEnTransito> buscarDonacionesAsignadas();

  void marcarDonacionListaParaEntregar(Long donacionId);

  void marcarDonacionEnCamino(Long donacionId, String linkMapa);

  void marcarDonacionEntregaExitosa(Long donacionId, String linkComprobante);

  void marcarDonacionErrorEntrega(Long donacionId, String motivo);

  void marcarDonacionVueltaDeposito(Long donacionId);
}
