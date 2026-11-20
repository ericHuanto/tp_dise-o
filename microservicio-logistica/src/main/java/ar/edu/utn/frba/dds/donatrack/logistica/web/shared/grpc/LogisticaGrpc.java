package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.grpc;

import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Coordenada;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.GpsRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers.CamionMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion.ActualizarCamionRequest;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion.CamionResponse;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonaciones;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonacionesGrpc;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.ServicioExternoException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class LogisticaGrpc extends LogisticaServiceGrpc.LogisticaServiceImplBase
    implements WithLogisticaPersistenceUnit {

  private final CamionRepository repoCamiones;
  private final GpsRepository repoGps;
  private final RutaRepository repoRutas;
  private final EntregaRepository repoEntregas;
  private final ConectorDonaciones conectorDonaciones;
  private final Path directorioFotos = Path.of("microservicio-logistica", "uploads", "entregas");

  public LogisticaGrpc() {
    this(
        CamionRepository.getInstancia(),
        GpsRepository.getInstancia(),
        RutaRepository.getInstancia(),
        EntregaRepository.getInstancia(),
        new ConectorDonacionesGrpc()
    );
  }

  public LogisticaGrpc(CamionRepository repoCamiones,
                       GpsRepository repoGps,
                       RutaRepository repoRutas,
                       EntregaRepository repoEntregas,
                       ConectorDonaciones conectorDonaciones
                       ) {
    this.repoCamiones = repoCamiones;
    this.repoGps = repoGps;
    this.repoRutas = repoRutas;
    this.repoEntregas = repoEntregas;
    this.conectorDonaciones = conectorDonaciones;

    try {
      if (!Files.exists(directorioFotos)) {
        Files.createDirectories(directorioFotos);
      }
    } catch (IOException e) {
      throw new RuntimeException("No se pudo crear la carpeta de fotos: " + directorioFotos.toAbsolutePath(), e);
    }
  }

  //CamionController.mostrarTodos()
  @Override
  public void obtenerVistaCamiones(VacioLogistica req, StreamObserver<VistaCamionesResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<Camion> camiones = repoCamiones.buscarTodos();
      long totalCamiones = camiones.size();
      long totalCamionesSinGps = camiones.stream().filter(c -> !c.tieneGps()).count();
      long totalCamionesEnRuta = repoRutas.camionesEnRutaAhora();

      VistaCamionesResponse.Builder builder = VistaCamionesResponse.newBuilder()
          .setTotalCamiones(totalCamiones)
          .setCamionesSinGps(totalCamionesSinGps)
          .setCamionesEnRuta(totalCamionesEnRuta);

      camiones.forEach(c -> builder.addCamiones(aCamionVistaMsg(c)));
      return builder.build();
    }));
  }

  //CamionController.mostrarFormularioCreacion()
  @Override
  public void listarGpsDisponibles(VacioLogistica req, StreamObserver<ListaGpsResponse> out) {
    responder(out, () -> withTransaction(() -> {
      ListaGpsResponse.Builder builder = ListaGpsResponse.newBuilder();
      for (Gps gps : repoGps.buscarTodos()) {
        builder.addGpsDisponibles(GpsMsg.newBuilder()
            .setImei(gps.getImei() != null ? gps.getImei() : "")
            .build());
      }
      return builder.build();
    }));
  }

  //CamionController.crear()
  @Override
  public void crearCamion(GuardarCamionRequest req, StreamObserver<CrearCamionResponse> out) {
    responder(out, () -> {
      // Si la patente ya existe, avisamos al BFF para que redirija con ?error=la_patente_ya_existe
      if (repoCamiones.buscarPorPatente(req.getPatente()) != null) {
        return CrearCamionResponse.newBuilder().setPatenteDuplicada(true).build();
      }

      withTransaction(() -> {
        Camion nuevoCamion = new Camion(
            req.getPatente(),
            req.getCapacidadVolumen(),
            req.getAltura(),
            req.getCapacidadCarga()
        );

        String imeiGps = req.getImeiGps();
        if (imeiGps != null && !imeiGps.trim().isEmpty()) {
          Gps gpsAsignado = repoGps.buscarPorId(imeiGps);
          Camion otroCamion = repoCamiones.buscarPorGps(imeiGps);
          if (otroCamion != null) {
            otroCamion.quitarGps();
            repoCamiones.actualizar(otroCamion);
          }
          nuevoCamion.agregarGps(gpsAsignado);
        } else {
          nuevoCamion.agregarGps(null);
        }

        repoCamiones.guardar(nuevoCamion);
      });

      return CrearCamionResponse.newBuilder().setPatenteDuplicada(false).build();
    });
  }

  //CamionController.mostrarFormularioEdicion()
  @Override
  public void obtenerDatosEdicionCamion(PatenteCamionRequest req,
                                        StreamObserver<DatosEdicionCamionResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Camion camion = repoCamiones.buscarPorPatente(req.getPatente());
      DatosEdicionCamionResponse.Builder builder = DatosEdicionCamionResponse.newBuilder();

      if (camion != null) {
        builder.setCamion(aCamionVistaMsg(camion));
      }

      for (Gps gps : repoGps.buscarTodos()) {
        builder.addGpsDisponibles(GpsMsg.newBuilder()
            .setImei(gps.getImei() != null ? gps.getImei() : "")
            .build());
      }
      return builder.build();
    }));
  }

  //CamionController.actualizar()
  @Override
  public void actualizarCamion(GuardarCamionRequest req, StreamObserver<ResultadoLogistica> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Camion camion = repoCamiones.buscarPorPatente(req.getPatente());
        String imeiGps = req.getImeiGps();
        ActualizarCamionRequest cuasiCamion = new ActualizarCamionRequest(
            req.getCapacidadVolumen(),
            req.getAltura(),
            req.getCapacidadCarga(),
            imeiGps
        );

        Gps gpsAsignado = null;
        if (imeiGps != null && !imeiGps.trim().isEmpty()) {
          gpsAsignado = repoGps.buscarPorId(imeiGps);
          Camion otroCamion = repoCamiones.buscarPorGps(imeiGps);
          if (otroCamion != null && !otroCamion.getPatente().equals(camion.getPatente())) {
            otroCamion.quitarGps();
            repoCamiones.actualizar(otroCamion);
          }
        }

        CamionMapper.actualizarDesdeRequest(camion, gpsAsignado, cuasiCamion);
        repoCamiones.actualizar(camion);
        entityManager().flush();
      });
      return ResultadoLogistica.newBuilder().setMensaje("Camión actualizado").build();
    });
  }

  //CamionController.eliminar()
  @Override
  public void eliminarCamion(PatenteCamionRequest req, StreamObserver<ResultadoLogistica> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Camion camion = repoCamiones.buscarPorPatente(req.getPatente());
        if (camion != null) {
          repoCamiones.eliminar(camion);
        }
      });
      return ResultadoLogistica.newBuilder().setMensaje("Camión eliminado").build();
    });
  }

  //ENTREGAS
  @Override
  public void listarEntregasEnTraslado(
      BeneficiarioEntregaRequest req,
      StreamObserver<ListaEntregasResponse> out) {

    responder(out, () -> withTransaction(() -> {

      Long idBeneficiario = req.getIdBeneficiario();

      List<Entrega> entregas =
          repoEntregas.buscarPorEstadoActual(TipoEstadoEntrega.EN_TRASLADO);

      ListaEntregasResponse.Builder builder = ListaEntregasResponse.newBuilder();

      for (Entrega e : entregas) {
        if (e.getDestino().getId().equals(idBeneficiario)) {
          builder.addEntregas(aEntregaVistaMsg(e));
        }
      }

      return builder.build();
    }));
  }
  @Override
  public void obtenerEntregaPorId(EntregaIdRequest req, StreamObserver<EntregaVistaMsg> out) {
    responder(out, () -> withTransaction(() -> {

      Entrega entrega = buscarEntregaPorId(req.getId());

      validarBeneficiarioEntrega(entrega, req.getIdBeneficiario());

      return aEntregaVistaMsg(entrega);
    }));
  }

  @Override
  public void confirmarRecepcion(
      ConfirmarRecepcionRequest req,
      StreamObserver<ResultadoLogistica> out) {

    responder(out, () -> {

      List<Long> idsDonaciones = withTransaction(() -> {

        Entrega entrega = buscarEntregaPorId(req.getIdEntrega());

        validarBeneficiarioEntrega(entrega, req.getIdBeneficiario());

        entrega.confirmarRecepcion();

        try {
          for (FotoRecepcionMsg foto : req.getFotosList()) {

            String nombreArchivo =
                UUID.randomUUID().toString().substring(0, 8)
                    + "-" + foto.getNombreArchivo();

            Path rutaDestino = directorioFotos.resolve(nombreArchivo);

            Files.write(rutaDestino, foto.getContenido().toByteArray());

            String url = "/uploads/entregas/" + nombreArchivo;

            entrega.agregarFotoRecepcion(url);
          }
        } catch (IOException e) {
          throw new RuntimeException(
              "Error al guardar las fotografías de recepción", e);
        }

        repoEntregas.actualizar(entrega);

        return entrega.getDonaciones().stream()
            .map(DonacionEnTransito::getId)
            .toList();
      });

      // Informamos a Donaciones mediante gRPC.
      String linkComprobante = "Entrega ID: " + req.getIdEntrega();

      try {
        for (Long idDonacion : idsDonaciones) {
          conectorDonaciones.marcarDonacionEntregaExitosa(
              idDonacion,
              linkComprobante
          );
        }
      } catch (ServicioExternoException e) {
        throw new RuntimeException(
            "La entrega fue confirmada en Logística, "
                + "pero no se pudo comunicar con Donaciones "
                + "para actualizar su estado.",
            e
        );
      }
      return ResultadoLogistica.newBuilder()
          .setMensaje("Recepción confirmada")
          .build();
    });
  }
  @Override
  public void obtenerCamionesDeDonaciones(DonacionesIdsRequest req, StreamObserver<ListaCamionesDonacionesResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Set<Long> idsBuscados = new HashSet<>(req.getIdsDonacionesList());
      Map<String, CamionDonacionesMsg.Builder> porPatente = new LinkedHashMap<>();

      // Iteramos sobre las rutas iniciadas en lugar de las entregas
      for (Ruta ruta : repoRutas.buscarTodas()) {
        if (!ruta.isEstaIniciada()) {
          continue;
        }

        Camion camion = ruta.getCamion();
        if (camion == null || camion.getUbicacionActual() == null) {
          continue;
        }

        for (Entrega entrega : ruta.getEntregasOrdenadas()) {
          if (entrega.getEstadoActual() != TipoEstadoEntrega.EN_TRASLADO) {
            continue;
          }

          List<Long> idsEnEntrega = entrega.getDonaciones().stream()
              .map(DonacionEnTransito::getId)
              .filter(idsBuscados::contains)
              .toList();

          if (!idsEnEntrega.isEmpty()) {
            Coordenada ubicacion = camion.getUbicacionActual();
            Beneficiario ben = entrega.getDestino();
            String destino = (ben != null && ben.getRazonSocial() != null) ? ben.getRazonSocial() : "Sin destino";
            String direccion = (ben != null && ben.getDireccion() != null) ? ben.getDireccion() : "Dirección no disponible";

            // Empaquetamos: "hace 3 min|Comedor america|Av. Rivadavia 10000, CABA"
            String infoConDestino = "hace 3 min|" + destino + "|" + direccion;

            porPatente.computeIfAbsent(camion.getPatente(), patente -> CamionDonacionesMsg.newBuilder()
                    .setPatente(patente != null ? patente : "")
                    .setLatitud(String.valueOf(ubicacion.getLatitud()))
                    .setLongitud(String.valueOf(ubicacion.getLongitud()))
                    .setUltimaActualizacion(infoConDestino))
                .addAllIdsDonaciones(idsEnEntrega);
          }
        }
      }

      ListaCamionesDonacionesResponse.Builder builder = ListaCamionesDonacionesResponse.newBuilder();
      porPatente.values().forEach(c -> builder.addCamiones(c.build()));
      return builder.build();
    }));
  }
  //Ruta
  @Override
  public void obtenerSeguimientoEntregas(VacioLogistica req, StreamObserver<ListaMarcadoresCamionResponse> out) {
    responder(out, () -> withTransaction(() -> {
      ListaMarcadoresCamionResponse.Builder builder = ListaMarcadoresCamionResponse.newBuilder();

      for(Ruta ruta : repoRutas.buscarTodas()) {
        if(!ruta.isEstaIniciada()){
          continue;
        }

        boolean tieneEntregasActivas = ruta.getEntregasOrdenadas()
            .stream()
            .anyMatch(entrega ->
                entrega.getEstadoActual() == TipoEstadoEntrega.EN_TRASLADO
            );

        if(!tieneEntregasActivas){
          continue;
        }

        Camion camion = ruta.getCamion();
        Coordenada ubicacion = camion.getUbicacionActual();

        if(ubicacion == null){
          continue;
        }

        builder.addMarcadores(MarcadorCamionMsg.newBuilder()
            .setPatente(camion.getPatente() != null ? camion.getPatente() : "")
            .setLatitud(String.valueOf(ubicacion.getLatitud()))
            .setLongitud(String.valueOf(ubicacion.getLongitud()))
            .setUltimaActualizacion("No disponible")
            .build());
      }
        return builder.build();
    }));
  }

  //================ FUNCIONES AUXILIARES ===============
  private CamionVistaMsg aCamionVistaMsg(Camion camion) {
    CamionResponse dto = CamionMapper.aDto(camion);

    return CamionVistaMsg.newBuilder()
        .setPatente(dto.patente() != null ? dto.patente() : "")
        .setCapacidadCarga(dto.capacidadCarga())
        .setCapacidadVolumen(dto.capacidadVolumen())
        .setAltura(dto.altura())
        .setImeiGps(dto.imeiGps() != null ? dto.imeiGps() : "")
        .setUltimaLatitud(dto.ultimaLatitud() != null ? String.valueOf(dto.ultimaLatitud()) : "")
        .setUltimaLongitud(dto.ultimaLongitud() != null ? String.valueOf(dto.ultimaLongitud()) : "")
        .build();
  }

  //T es tipo genérico
  //Supplier<T> es una función que no recibe parámetros y devuelve un objeto tipo T.
  private <T> void responder(StreamObserver<T> out, Supplier<T> accion) {
    try {
      out.onNext(accion.get());
      out.onCompleted();

    } catch (Throwable e) {
      e.printStackTrace();

      if (e instanceof io.grpc.StatusRuntimeException) {
        out.onError(e);
      } else {
        out.onError(Status.INTERNAL
            .withDescription("Error en logística: " + e.getMessage())
            .asRuntimeException());
      }

    } finally {
      WithLogisticaPersistenceUnit.dispose();
    }
  }
  //ENTREGAS
  private EntregaVistaMsg aEntregaVistaMsg(Entrega entrega) {

    List<Long> idsDonaciones = entrega.getDonaciones().stream()
        .map(DonacionEnTransito::getId)
        .toList();

    return EntregaVistaMsg.newBuilder()
        .setId(entrega.getId())
        .setEstadoActual(entrega.getEstadoActual() != null
            ? entrega.getEstadoActual().name()
            : "")
        .setPatenteCamion(entrega.getCamionAsignado() != null
            ? entrega.getCamionAsignado().getPatente()
            : "")
        .addAllIdsDonaciones(idsDonaciones)
        .build();
  }
  private Entrega buscarEntregaPorId (Long id) {
    Entrega entrega = repoEntregas.buscarPorId(id);
    if(entrega == null){
      throw new RuntimeException("Entrega no encontrada: " + id);
    }
    return entrega;
  }

  private void validarBeneficiarioEntrega(
      Entrega entrega,
      Long idBeneficiario
  ) {
    if (entrega.getDestino() == null ||
        !entrega.getDestino().getId().equals(idBeneficiario)) {

      throw Status.PERMISSION_DENIED
          .withDescription("La entrega no pertenece al beneficiario")
          .asRuntimeException();
    }
  }
}