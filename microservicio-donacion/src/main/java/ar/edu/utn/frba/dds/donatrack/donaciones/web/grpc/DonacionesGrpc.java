package ar.edu.utn.frba.dds.donatrack.donaciones.web.grpc;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.DispatcherEventos;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoEntregaExitosa;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoEntregaFallida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoInicioDeRuta;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionId;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.EnCaminoRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.EntregadaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ErrorEntregaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaDonaciones;
import ar.edu.utn.frba.dds.donatrack.grpc.Resultado;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BodyException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.CambioDeEstadoNoPermitidoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.function.Supplier;

public class DonacionesGrpc extends DonacionesServiceGrpc.DonacionesServiceImplBase
    implements WithSimplePersistenceUnit {
  private final DonacionRepository repoDonaciones;

  public DonacionesGrpc(DonacionRepository repoDonaciones) {
    this.repoDonaciones = repoDonaciones;
  }

  @Override
  public void buscarDonacionesAsignadas(Vacio req, StreamObserver<ListaDonaciones> out) {
    responder(out, () -> withTransaction(() -> {
      ListaDonaciones.Builder lista = ListaDonaciones.newBuilder();
      repoDonaciones.buscarTodoPorEstado(TipoEstadoDonacion.ASIGNACION_REALIZADA)
          .forEach(d -> lista.addDonaciones(aMsg(d)));
      return lista.build();
    }));
  }

  // [Asignación Realizada] -> [Lista Para Entregar]
  @Override
  public void marcarListaParaEntregar(DonacionId req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.listaParaEntregar();
        repoDonaciones.actualizar(donacion);
      });
      return ok("la donación esta lista para ser entregada");
    });
  }

  // [Lista Para Entregar] -> [En Traslado]
  @Override
  public void marcarEnCamino(EnCaminoRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      exigir(req.getLinkMapa(), "link_mapa");
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.enCamino();
        repoDonaciones.actualizar(donacion);
        DispatcherEventos.getInstancia().publicar(new EventoInicioDeRuta(
            donacion.getBeneficiario(), donacion.getDonantes(),
            donacion.getDescripcion(), req.getLinkMapa()));
      });
      return ok("la donación se encuentra en traslado");
    });
  }

  // [En Traslado] -> [Entregada]
  @Override
  public void marcarEntregada(EntregadaRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      exigir(req.getLinkComprobanteEntrega(), "link_comprobante_entrega");
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.entregada();
        repoDonaciones.actualizar(donacion);
        DispatcherEventos.getInstancia().publicar(new EventoEntregaExitosa(
            donacion.getBeneficiario(), donacion.getDonantes(),
            donacion.getDescripcion(), req.getLinkComprobanteEntrega()));
      });
      return ok("la donación fue entregada");
    });
  }

  // [En Traslado] -> [Entrega Fallida]
  @Override
  public void marcarErrorEntrega(ErrorEntregaRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      exigir(req.getObservacion(), "observacion");
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.errorAlEntregar(req.getObservacion());
        repoDonaciones.actualizar(donacion);
        DispatcherEventos.getInstancia().publicar(
            new EventoEntregaFallida(donacion, req.getObservacion()));
      });
      return ok("la donación No pudo ser entregada");
    });
  }

  // [Entrega Fallida] -> [En Depósito]
  @Override
  public void marcarVueltaDeposito(DonacionId req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.retornarADeposito();
        repoDonaciones.actualizar(donacion);
      });
      return ok("la donación fue devuelta a deposito");
    });
  }

  //================ FUNCIONES AUXILIARES ===============
  private <T> void responder(StreamObserver<T> out, Supplier<T> accion) {
    try {
      out.onNext(accion.get());
      out.onCompleted();
    } catch (Exception e) {
      out.onError(aStatus(e).asRuntimeException());
    } finally {
      // el EntityManager es por hilo y gRPC reutiliza hilos: se libera para no arrastrar caché
      WithSimplePersistenceUnit.dispose();
    }
  }

  private Status aStatus(Exception e) {
    if (e instanceof RecursoNoEncontradoException) return Status.NOT_FOUND.withDescription(e.getMessage());
    if (e instanceof BodyException || e instanceof DominioException)
      return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
    if (e instanceof CambioDeEstadoNoPermitidoException)
      return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
    e.printStackTrace();
    return Status.INTERNAL.withDescription("Error interno del servidor");
  }

  private DonacionMsg aMsg(Donacion d) {
    Beneficiario b = d.getBeneficiario();
    DonacionMsg.Builder msg = DonacionMsg.newBuilder()
        .setId(d.getId())
        .setDescripcion(d.getDescripcion());
    if (b != null) {
      msg.setBeneficiario(BeneficiarioMsg.newBuilder()
          .setId(b.getId())
          .setRazonSocial(b.getRazonSocial())
          .setDireccion(b.getDireccion()));
    }
    return msg.build();
  }

  private Resultado ok(String mensaje) {
    return Resultado.newBuilder().setMensaje(mensaje).build();
  }

  private void exigir(String valor, String campo) {
    if (valor == null || valor.isEmpty()) throw new BodyException("Bad Request, necesita: '" + campo + "'");
  }

  private Donacion buscarDonacionPorId(Long id) {
    Donacion donacion = repoDonaciones.buscarPorId(id);
    if (donacion == null) throw new RecursoNoEncontradoException("No existe donación: " + id);
    return donacion;
  }
}