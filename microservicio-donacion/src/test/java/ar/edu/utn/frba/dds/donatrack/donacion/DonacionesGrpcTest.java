package ar.edu.utn.frba.dds.donatrack.donacion;

import ar.edu.utn.frba.dds.donatrack.builder.BienBuilder;
import ar.edu.utn.frba.dds.donatrack.builder.CategoriaBuilder;
import ar.edu.utn.frba.dds.donatrack.builder.PersonaHumanaBuilder;
import ar.edu.utn.frba.dds.donatrack.builder.SubcategoriaBuilder;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Categoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.grpc.DonacionesGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.EnCaminoRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.EntregadaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ErrorEntregaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonaciones;
import ar.edu.utn.frba.dds.donatrack.grpc.Resultado;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DonacionesGrpcTest implements WithSimplePersistenceUnit {

  private Server server;
  private ManagedChannel channel;
  private DonacionesServiceGrpc.DonacionesServiceBlockingStub stub;
  private DonacionRepository repoDonaciones;

  private Bien fideosLucetti;
  private Donante donantePrueba;

  @BeforeEach
  public void setUp() throws Exception {
    repoDonaciones = DonacionRepository.getInstancia();

    withTransaction(() -> {
      Categoria alimentos = new CategoriaBuilder().conNombre("Alimentos").build();
      entityManager().persist(alimentos);

      Subcategoria fideos = new SubcategoriaBuilder()
          .conNombre("Fideos secos")
          .conCategoria(alimentos)
          .build();
      entityManager().persist(fideos);

      fideosLucetti = new BienBuilder()
          .conDescripcion("Fideos Lucchetti")
          .conCantidad(4)
          .conSubcategoria(fideos)
          .conFechaVencimiento(LocalDate.now().plusMonths(6))
          .conUnidad(UnidadMedida.UNIDADES)
          .buildPerecedero();

      donantePrueba = new PersonaHumanaBuilder()
          .conNombre("Juan")
          .conApellido("Pérez")
          .conDocumento(new Documento(TipoDocumento.DNI, "12345678"))
          .conAgregarContacto(new CorreoDeContacto("juan@prueba.com", true))
          .conDireccion("alguna dirección")
          .build();
      entityManager().persist(donantePrueba);
    });

    String serverName = InProcessServerBuilder.generateName();
    server = InProcessServerBuilder.forName(serverName)
        .directExecutor()
        .addService(new DonacionesGrpc(repoDonaciones))
        .build()
        .start();

    channel = InProcessChannelBuilder.forName(serverName)
        .directExecutor()
        .build();
    stub = DonacionesServiceGrpc.newBlockingStub(channel);
  }

  @AfterEach
  public void tearDown() {
    channel.shutdownNow();
    server.shutdownNow();
  }

  // ================= TESTS DE ÉXITO (TRANSICIONES) ================= //

  @Test
  public void testBuscarDonacionesAsignadas() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.ASIGNACION_REALIZADA);

    ListaDonaciones respuesta = stub.buscarDonacionesAsignadas(Vacio.getDefaultInstance());

    boolean encontrada = respuesta.getDonacionesList().stream()
        .anyMatch(d -> d.getId() == donacion.getId());
    assertEquals(true, encontrada, "La donación asignada debería estar en la respuesta");
  }

  @Test
  public void testMarcarListaParaEntregar_Exito() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.ASIGNACION_REALIZADA);

    Resultado res = stub.marcarListaParaEntregar(DonacionId.newBuilder().setId(donacion.getId()).build());

    assertEquals("la donación esta lista para ser entregada", res.getMensaje());
    Donacion dbDonacion = repoDonaciones.buscarPorId(donacion.getId());
    assertEquals(TipoEstadoDonacion.LISTA_PARA_ENTREGAR, dbDonacion.getEstadoActual());
  }

  @Test
  public void testMarcarEnCamino_Exito() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.LISTA_PARA_ENTREGAR);

    Resultado res = stub.marcarEnCamino(EnCaminoRequest.newBuilder()
        .setId(donacion.getId())
        .setLinkMapa("http://maps.google.com/test")
        .build());

    assertEquals("la donación se encuentra en traslado", res.getMensaje());
    Donacion dbDonacion = repoDonaciones.buscarPorId(donacion.getId());
    assertEquals(TipoEstadoDonacion.EN_TRASLADO, dbDonacion.getEstadoActual());
  }

  @Test
  public void testMarcarEntregada_Exito() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.EN_TRASLADO);

    Resultado res = stub.marcarEntregada(EntregadaRequest.newBuilder()
        .setId(donacion.getId())
        .setLinkComprobanteEntrega("http://comprobante.com/test")
        .build());

    assertEquals("la donación fue entregada", res.getMensaje());
    Donacion dbDonacion = repoDonaciones.buscarPorId(donacion.getId());
    assertEquals(TipoEstadoDonacion.ENTREGADA, dbDonacion.getEstadoActual());
  }

  @Test
  public void testMarcarErrorEntrega_Exito() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.EN_TRASLADO);

    Resultado res = stub.marcarErrorEntrega(ErrorEntregaRequest.newBuilder()
        .setId(donacion.getId())
        .setObservacion("No había nadie en casa")
        .build());

    assertEquals("la donación No pudo ser entregada", res.getMensaje());
    Donacion dbDonacion = repoDonaciones.buscarPorId(donacion.getId());
    assertEquals(TipoEstadoDonacion.ENTREGA_FALLIDA, dbDonacion.getEstadoActual());
  }

  @Test
  public void testMarcarVueltaDeposito_Exito() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.ENTREGA_FALLIDA);

    Resultado res = stub.marcarVueltaDeposito(DonacionId.newBuilder().setId(donacion.getId()).build());

    assertEquals("la donación fue devuelta a deposito", res.getMensaje());
    Donacion dbDonacion = repoDonaciones.buscarPorId(donacion.getId());
    assertEquals(TipoEstadoDonacion.EN_DEPOSITO, dbDonacion.getEstadoActual());
  }

  // ================= TESTS DE ERROR ================= //

  @Test
  public void testErrorNoEncontrado_StatusNotFound() {
    StatusRuntimeException excepcion = assertThrows(StatusRuntimeException.class, () ->
        stub.marcarListaParaEntregar(DonacionId.newBuilder().setId(9999L).build())
    );

    assertEquals(Status.NOT_FOUND.getCode(), excepcion.getStatus().getCode());
  }

  @Test
  public void testErrorBodyIncompleto_InvalidArgument() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.LISTA_PARA_ENTREGAR);

    StatusRuntimeException excepcion = assertThrows(StatusRuntimeException.class, () ->
        stub.marcarEnCamino(EnCaminoRequest.newBuilder()
            .setId(donacion.getId())
            .setLinkMapa("")
            .build())
    );

    assertEquals(Status.INVALID_ARGUMENT.getCode(), excepcion.getStatus().getCode());
  }

  @Test
  public void testErrorCambioDeEstadoInvalido_FailedPrecondition() {
    Donacion donacion = crearDonacionEnEstado(TipoEstadoDonacion.EN_DEPOSITO);

    StatusRuntimeException excepcion = assertThrows(StatusRuntimeException.class, () ->
        stub.marcarEnCamino(EnCaminoRequest.newBuilder()
            .setId(donacion.getId())
            .setLinkMapa("http://link.com")
            .build())
    );

    assertEquals(Status.FAILED_PRECONDITION.getCode(), excepcion.getStatus().getCode());
  }

  // ================= UTILIDADES ================= //

  private Donacion crearDonacionEnEstado(TipoEstadoDonacion estadoBuscado) {
    Donacion donacion = new Donacion(
        new ArrayList<>(List.of(fideosLucetti)),
        new ArrayList<>(List.of(donantePrueba))
    );

    withTransaction(() -> {
      repoDonaciones.guardar(donacion);

      if (estadoBuscado == TipoEstadoDonacion.EN_DEPOSITO) return;

      Beneficiario beneficiario = new Beneficiario(
          "Razon Social",
          "Calle Falsa 123",
          new ArrayList<>(List.of(new CorreoDeContacto("test@prueba.com", true)))
      );
      entityManager().persist(beneficiario);

      donacion.asignarA(beneficiario);

      if (estadoBuscado == TipoEstadoDonacion.ASIGNACION_REALIZADA) {
        repoDonaciones.actualizar(donacion);
        return;
      }

      donacion.listaParaEntregar();
      if (estadoBuscado == TipoEstadoDonacion.LISTA_PARA_ENTREGAR) {
        repoDonaciones.actualizar(donacion);
        return;
      }

      donacion.enCamino();
      if (estadoBuscado == TipoEstadoDonacion.EN_TRASLADO) {
        repoDonaciones.actualizar(donacion);
        return;
      }

      if (estadoBuscado == TipoEstadoDonacion.ENTREGADA) {
        donacion.entregada();
      } else if (estadoBuscado == TipoEstadoDonacion.ENTREGA_FALLIDA) {
        donacion.errorAlEntregar("Fallo forzado");
      }

      repoDonaciones.actualizar(donacion);
    });

    return donacion;
  }
}