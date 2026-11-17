package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.EnCaminoRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.EntregadaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ErrorEntregaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonaciones;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.ServicioExternoException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ConectorDonacionesGrpc implements ConectorDonaciones {
  private static final int TIMEOUT_SEGUNDOS = 10;
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub stub;

  public ConectorDonacionesGrpc() {
    this(ManagedChannelBuilder
        .forAddress(
            ConfiguracionEntorno.getInstance().hostGrpcDonaciones("localhost"),
            ConfiguracionEntorno.getInstance().puertoGrpcDonaciones(7072))
        .usePlaintext()
        .build());
  }

  // Constructor para tests: recibe un canal (por ejemplo, in-process)
  public ConectorDonacionesGrpc(ManagedChannel canal) {
    this.stub = DonacionesServiceGrpc.newBlockingStub(canal);
  }

  @Override
  public List<DonacionEnTransito> buscarDonacionesAsignadas() {
    ListaDonaciones respuesta = llamar("buscar donaciones asignadas",
        () -> conDeadline().buscarDonacionesAsignadas(Vacio.getDefaultInstance()));

    Map<Long, Beneficiario> beneficiariosCache = new HashMap<>();
    return respuesta.getDonacionesList().stream()
        .map(d -> aDominio(d, beneficiariosCache))
        .toList();
  }

  //activa el paso 3 de la donacion (Lista Para Entregar)
  @Override
  public void marcarDonacionListaParaEntregar(Long donacionId) {
    llamar("marcar lista para entregar la donación " + donacionId, () ->
        conDeadline().marcarListaParaEntregar(DonacionId.newBuilder().setId(donacionId).build()));
  }

  //activa el paso 4 de la donacion (En Traslado)
  @Override
  public void marcarDonacionEnCamino(Long donacionId, String linkMapa) {
    llamar("marcar en camino la donación " + donacionId, () ->
        conDeadline().marcarEnCamino(EnCaminoRequest.newBuilder()
            .setId(donacionId)
            .setLinkMapa(linkMapa == null ? "" : linkMapa)
            .build()));
  }

  //activa el paso 5 de la donacion (Entregada)
  @Override
  public void marcarDonacionEntregaExitosa(Long donacionId, String linkComprobante) {
    llamar("marcar entregada la donación " + donacionId, () ->
        conDeadline().marcarEntregada(EntregadaRequest.newBuilder()
            .setId(donacionId)
            .setLinkComprobanteEntrega(linkComprobante == null ? "" : linkComprobante)
            .build()));
  }

  //activa el paso 5B de la donacion (Error Al Entregar)
  @Override
  public void marcarDonacionErrorEntrega(Long donacionId, String motivo) {
    llamar("marcar error de entrega en la donación " + donacionId, () ->
        conDeadline().marcarErrorEntrega(ErrorEntregaRequest.newBuilder()
            .setId(donacionId)
            .setObservacion(motivo == null ? "" : motivo)
            .build()));
  }

  //activa el paso 6B de la donacion (Devuelta A Deposito)
  @Override
  public void marcarDonacionVueltaDeposito(Long donacionId) {
    llamar("marcar vuelta a depósito la donación " + donacionId, () ->
        conDeadline().marcarVueltaDeposito(DonacionId.newBuilder().setId(donacionId).build()));
  }

  //===================== FUNCIONES AUXILIARES =======================
  // El deadline se aplica por llamada, no al stub completo
  private DonacionesServiceGrpc.DonacionesServiceBlockingStub conDeadline() {
    return stub.withDeadlineAfter(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
  }

  private <T> T llamar(String operacion, Supplier<T> llamada) {
    try {
      return llamada.get();
    } catch (StatusRuntimeException e) {
      throw new ServicioExternoException("Error al " + operacion + " en servicio de donaciones: "
          + e.getStatus().getCode() + " — " + e.getStatus().getDescription(), e);
    }
  }

  private DonacionEnTransito aDominio(DonacionMsg msg, Map<Long, Beneficiario> cache) {
    if (!msg.hasBeneficiario()) {
      throw new ServicioExternoException("Donación asignada sin beneficiario: id=" + msg.getId());
    }
    BeneficiarioMsg b = msg.getBeneficiario();
    Beneficiario beneficiario = cache.computeIfAbsent(b.getId(),
        id -> new Beneficiario(id, b.getRazonSocial(), b.getDireccion()));
    return new DonacionEnTransito(msg.getId(), msg.getDescripcion(), beneficiario);
  }
}