package ar.edu.utn.frba.dds.donatrack.usuario.web.config;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientsConfig {
  public static final int PUERTO_GRPC_DONACIONES_DEFAULT = 7072;
  public static final int PUERTO_GRPC_LOGISTICA_DEFAULT = 7073;

  private final ManagedChannel canalDonaciones;
  private final ManagedChannel canalLogistica;
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;
  private final LogisticaServiceGrpc.LogisticaServiceBlockingStub logisticaStub;

  public GrpcClientsConfig() {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    // En local usa localhost. En Docker tomará el nombre del contenedor desde las variables de entorno
    String hostDonaciones = config.hostGrpcDonaciones("localhost");
    int portDonaciones = config.puertoGrpcDonaciones(PUERTO_GRPC_DONACIONES_DEFAULT);

    String hostLogistica = config.hostGrpcLogistica("localhost");
    int portLogistica = config.puertoGrpcLogistica(PUERTO_GRPC_LOGISTICA_DEFAULT);

    this.canalDonaciones = ManagedChannelBuilder
        .forAddress(hostDonaciones, portDonaciones)
        .usePlaintext()
        .build();

    this.canalLogistica = ManagedChannelBuilder
        .forAddress(hostLogistica, portLogistica)
        .usePlaintext()
        .build();

    this.donacionesStub = DonacionesServiceGrpc.newBlockingStub(this.canalDonaciones);
    this.logisticaStub = LogisticaServiceGrpc.newBlockingStub(this.canalLogistica);
  }

  public DonacionesServiceGrpc.DonacionesServiceBlockingStub getDonacionesStub() {
    return this.donacionesStub;
  }

  public LogisticaServiceGrpc.LogisticaServiceBlockingStub getLogisticaStub() {
    return this.logisticaStub;
  }

  public void cerrarCanales() {
    if (canalDonaciones != null && !canalDonaciones.isShutdown()) {
      canalDonaciones.shutdown();
    }
    if (canalLogistica != null && !canalLogistica.isShutdown()) {
      canalLogistica.shutdown();
    }
  }
}