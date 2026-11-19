package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.grpc;

import static ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.ContactoMapper.aDominio;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.BienFactory;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.DispatcherEventos;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoEntregaExitosa;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoEntregaFallida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.eventos.EventoInicioDeRuta;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.EstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.SmsDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.WhatsappDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.segmentador.SegmentadorDonaciones;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.Genero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.Juridica;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.Representante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.TipoOrganizacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.persona.Humana;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.FilaError;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ImportadorCsv;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadordonantes.importadorcsv.ResultadoImportacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.Ranking;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.MedioContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Frecuencia;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.TipoNecesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.CategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.RankingRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.RegistroEntregaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.BeneficiarioMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.ContactoMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.DonacionMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.NecesidadMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.RankingMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioMapaResponse;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.contacto.ContactoDto;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.necesidad.NecesidadRequest;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.necesidad.NecesidadResponse;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.vista.DonacionVistaMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.vista.ResumenDonacionesMapper;
import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.BodyException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.CambioDeEstadoNoPermitidoException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import com.google.gson.Gson;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class DonacionesGrpc extends DonacionesServiceGrpc.DonacionesServiceImplBase
    implements WithSimplePersistenceUnit {
  private static final DateTimeFormatter FECHA_HORA_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private final DonacionRepository repoDonaciones;
  private final DonanteRepository repoDonantes;
  private final BeneficiarioRepository repoBeneficiarios;
  private final CategoriaRepository repoCategorias;
  private final SubcategoriaRepository repoSubcategorias;
  private final RankingRepository repoRankings;
  private final RegistroEntregaRepository repoRegistros = RegistroEntregaRepository.getInstancia();
  private final SegmentadorDonaciones segmentador = new SegmentadorDonaciones();

  public DonacionesGrpc(DonacionRepository repoDonaciones) {
    this.repoDonaciones = repoDonaciones;
    this.repoDonantes = DonanteRepository.getInstancia();
    this.repoBeneficiarios = BeneficiarioRepository.getInstancia();
    this.repoCategorias = CategoriaRepository.getInstancia();
    this.repoSubcategorias = SubcategoriaRepository.getInstancia();
    this.repoRankings = RankingRepository.getInstancia();

  }

  public DonacionesGrpc(DonacionRepository repoDonaciones,
                        DonanteRepository repoDonantes,
                        BeneficiarioRepository repoBeneficiarios,
                        CategoriaRepository repoCategorias,
                        SubcategoriaRepository repoSubcategorias,
                        RankingRepository repoRankings) {
    this.repoDonaciones = repoDonaciones;
    this.repoDonantes = repoDonantes;
    this.repoBeneficiarios = repoBeneficiarios;
    this.repoCategorias = repoCategorias;
    this.repoSubcategorias = repoSubcategorias;
    this.repoRankings = repoRankings;
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

  //Registro Persona Humana
  @Override
  public void registrarDonanteHumana(RegistrarHumanaRequest req, StreamObserver<RegistroActorResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Documento doc = new Documento(TipoDocumento.valueOf(req.getTipoDocumento()), req.getNumeroDocumento());
      LocalDate fechaNac = LocalDate.parse(req.getFechaNacimiento());
      Genero genero = Genero.valueOf(req.getGenero());
      List<MedioContacto> contactos = aDominioContactos(req.getContactosList());

      Humana nuevaHumana = new Humana(
          req.getNombre(),
          req.getApellido(),
          doc,
          fechaNac,
          genero,
          req.getDireccion(),
          contactos
      );
      repoDonantes.guardar(nuevaHumana);

      return RegistroActorResponse.newBuilder()
          .setIdCreado(nuevaHumana.getId())
          .build();
    }));
  }

  //Registro Persona jurídica
  @Override
  public void registrarDonanteJuridica(RegistrarJuridicaRequest req, StreamObserver<RegistroActorResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Documento docJuridica = new Documento(TipoDocumento.valueOf(req.getTipoDocumento()), req.getNumeroDocumento());
      TipoOrganizacion tipoOrg = TipoOrganizacion.valueOf(req.getTipoOrganizacion());

      Documento docRep = new Documento(
          TipoDocumento.valueOf(req.getTipoDocumentoRepresentante()),
          req.getNumeroDocumentoRepresentante()
      );
      List<MedioContacto> contactosRep = aDominioContactos(req.getContactosList());

      Representante representante = new Representante(
          req.getNombreRepresentante(),
          req.getApellidoRepresentante(),
          docRep,
          req.getDireccionRepresentante(),
          contactosRep
      );

      Juridica nuevaJuridica = new Juridica(
          req.getRazonSocial(),
          docJuridica,
          tipoOrg,
          req.getRubro(),
          List.of(representante)
      );
      repoDonantes.guardar(nuevaJuridica);

      return RegistroActorResponse.newBuilder()
          .setIdCreado(nuevaJuridica.getId())
          .build();
    }));
  }

  //Registro Beneficiario
  @Override
  public void registrarBeneficiario(RegistrarBeneficiarioRequest req, StreamObserver<RegistroActorResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<MedioContacto> contactos = aDominioContactos(req.getContactosList());
      Beneficiario nuevoBeneficiario = new Beneficiario(
          req.getRazonSocial(),
          req.getDireccion(),
          contactos
      );
      repoBeneficiarios.guardar(nuevoBeneficiario);

      return RegistroActorResponse.newBuilder()
          .setIdCreado(nuevoBeneficiario.getId())
          .build();
    }));
  }

  //Mapa de Beneficiarios
  @Override
  public void obtenerDatosMapaDonacionesRecibidas(Vacio req, StreamObserver<DatosMapaResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<Beneficiario> beneficiarios = repoBeneficiarios.buscarBeneficiariosConDonacionesRecibidas();
      List<BeneficiarioMapaResponse> beneficiariosDto = BeneficiarioMapper.aDtoMapa(beneficiarios);
      String beneficiariosJson = new Gson().toJson(beneficiariosDto);

      List<CategoriaMsg> categoriasProto = repoCategorias.buscarTodos().stream()
          .map(c -> CategoriaMsg.newBuilder()
              .setId(c.getId())
              .setNombre(c.getNombre())
              .build())
          .toList();

      return DatosMapaResponse.newBuilder()
          .setMarcadoresJson(beneficiariosJson)
          .addAllCategorias(categoriasProto)
          .build();
    }));
  }

  //
  @Override
  public void obtenerVistaDonacionesDonante(FiltroDonacionesDonanteRequest req,
                                            StreamObserver<VistaDonacionesDonanteResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Donante donante = repoDonantes.buscarPorId(req.getIdDonante());
      if (donante == null) {
        throw new RecursoNoEncontradoException("No existe el donante con id: " + req.getIdDonante());
      }

      TipoEstadoDonacion estado = req.getEstado().isBlank()
          ? null
          : TipoEstadoDonacion.valueOf(req.getEstado());
      Long idCategoria = req.getIdCategoria() == 0 ? null : req.getIdCategoria();
      List<Long> idsSubcategorias = req.getIdsSubcategoriasList();

      List<Donacion> donaciones = repoDonaciones.buscarPorDonante(
          donante, estado, idCategoria, idsSubcategorias
      );

      VistaDonacionesDonanteResponse.Builder builder = VistaDonacionesDonanteResponse.newBuilder();

      for (Donacion d : donaciones) {
        DonacionVistaMsg.Builder dMsg = DonacionVistaMsg.newBuilder()
            .setId(d.getId())
            .setDescripcion(d.getDescripcion() != null ? d.getDescripcion() : "")
            .setEstadoActual(d.getEstadoActual() != null ? d.getEstadoActual().name() : "");
        if(d.getSubcategoria() != null) {
          dMsg.setSubcategoria(aSubcategoriaMsg(d.getSubcategoria()));
        }
        builder.addDonaciones(dMsg.build());
      }

      Arrays.stream(TipoEstadoDonacion.values())
          .forEach(e -> builder.addEstados(e.name()));

      repoCategorias.buscarTodos().forEach(c ->
          builder.addCategorias(CategoriaMsg.newBuilder()
              .setId(c.getId())
              .setNombre(c.getNombre())
              .build())
      );

      repoSubcategorias.buscarTodos().forEach(s ->
          builder.addSubcategorias(aSubcategoriaMsg(s))
      );

      return builder.build();
    }));
  }

  @Override
  public void listarBeneficiariosParaDonante(Vacio req,
                                             StreamObserver<ListaBeneficiariosParaDonanteResponse> out) {
    responder(out, () -> withTransaction(() -> {
      ListaBeneficiariosParaDonanteResponse.Builder builder = ListaBeneficiariosParaDonanteResponse.newBuilder();
      repoBeneficiarios.buscarTodos().forEach(b -> builder.addBeneficiarios(aBeneficiarioDetalleMsg(b)));
      return builder.build();
    }));
  }

  @Override
  public void obtenerDetalleBeneficiario(BeneficiarioIdRequest req,
                                         StreamObserver<DetalleBeneficiarioResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Beneficiario b = repoBeneficiarios.buscarPorId(req.getId());
      if (b == null) {
        return DetalleBeneficiarioResponse.newBuilder().setEncontrado(false).build();
      }
      return DetalleBeneficiarioResponse.newBuilder()
          .setEncontrado(true)
          .setBeneficiario(aBeneficiarioDetalleMsg(b))
          .build();
    }));
  }

  //Donacion
  @Override
  public void listarDonacionesAdmin(FiltroDonacionesAdminRequest req,
                                    StreamObserver<ListaDonacionesAdminResponse> out) {
    responder(out, () -> withTransaction(() -> {
      String q = req.getQ().trim();
      String categoria = req.getCategoria().trim();
      String estado = req.getEstado().trim();
      String vencimiento = req.getVencimiento().isBlank() ? "todos" : req.getVencimiento().trim();
      LocalDate hoy = LocalDate.now();

      List<Donacion> filtradas = repoDonaciones.buscarTodos().stream()
          .filter(d -> coincideBusqueda(d, q))
          .filter(d -> categoria.isEmpty() || coincideCategoria(d, categoria))
          .filter(d -> estado.isEmpty() || d.getEstadoActual().name().equalsIgnoreCase(estado))
          .filter(d -> coincideVencimiento(d, vencimiento, hoy))
          .sorted(Comparator
              .comparing(DonacionVistaMapper::vencimientoDe, Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(Donacion::getId))
          .toList();

      ListaDonacionesAdminResponse.Builder builder = ListaDonacionesAdminResponse.newBuilder();
      filtradas.forEach(d -> builder.addDonaciones(aDonacionAdminFilaMsg(d)));

      repoSubcategorias.buscarTodas().stream()
          .map(s -> s.getCategoria().getNombre())
          .distinct()
          .sorted()
          .forEach(builder::addCategorias);

      return builder.build();
    }));
  }

  @Override
  public void marcarDonacionComoVencida(DonacionId req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Donacion donacion = buscarDonacionPorId(req.getId());
        donacion.vencida();
        repoDonaciones.actualizar(donacion);
      });
      return ok("La donación fue marcada como vencida");
    });
  }

  //Necesidad
  @Override
  public void listarNecesidades(ListarNecesidadesRequest req,
                                StreamObserver<ListarNecesidadesResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<Necesidad> necesidades = new ArrayList<>();

      if (req.getEsAdmin()) {
        for (Beneficiario beneficiario : repoBeneficiarios.buscarTodos()) {
          necesidades.addAll(beneficiario.getNecesidades());
        }
      } else {
        Beneficiario beneficiario = repoBeneficiarios.buscarPorId(req.getIdBeneficiario());
        if (beneficiario == null) {
          throw new RecursoNoEncontradoException("No existe un beneficiario asociado a este usuario");
        }
        necesidades.addAll(beneficiario.getNecesidades());
      }

      List<NecesidadResponse> dtos = NecesidadMapper.aDto(necesidades);
      ListarNecesidadesResponse.Builder builder = ListarNecesidadesResponse.newBuilder();

      for (NecesidadResponse dto : dtos) {
        builder.addNecesidades(NecesidadResponseMsg.newBuilder()
            .setId(dto.id())
            .setTipo(dto.tipo() != null ? dto.tipo() : "")
            .setDescripcion(dto.descripcion() != null ? dto.descripcion() : "")
            .setUnidadMedida(dto.unidadMedida() != null ? dto.unidadMedida() : "")
            .setCategoria(dto.categoria() != null ? dto.categoria() : "")
            .setSubcategoria(dto.subcategoria() != null ? dto.subcategoria() : "")
            .setCantidadRecibida(dto.cantidadRecibida() != null ? dto.cantidadRecibida() : 0)
            .setEstaSatisfecha(Boolean.TRUE.equals(dto.estaSatisfecha()))
            .setCantidadPorPeriodo(dto.cantidadPorPeriodo() != null ? dto.cantidadPorPeriodo() : 0)
            .setPeriodo(dto.periodo() != null ? dto.periodo() : "")
            .setCantidadRequerida(dto.cantidadRequerida() != null ? dto.cantidadRequerida() : 0)
            .setFechaRegistrada(dto.fechaRegistrada() != null ? dto.fechaRegistrada().toString() : "")
            .build());
      }

      return builder.build();
    }));
  }

  @Override
  public void obtenerDatosFormularioNecesidad(Vacio req,
                                              StreamObserver<DatosFormularioNecesidadResponse> out) {
    responder(out, () -> withTransaction(() -> {
      DatosFormularioNecesidadResponse.Builder builder = DatosFormularioNecesidadResponse.newBuilder();

      repoCategorias.buscarTodos().forEach(c ->
          builder.addCategorias(CategoriaMsg.newBuilder()
              .setId(c.getId())
              .setNombre(c.getNombre())
              .build())
      );

      repoSubcategorias.buscarTodos().forEach(s ->
          builder.addSubcategorias(aSubcategoriaMsg(s))
      );

      return builder.build();
    }));
  }

  @Override
  public void guardarNecesidad(GuardarNecesidadRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Beneficiario beneficiario = repoBeneficiarios.buscarPorId(req.getIdBeneficiario());
        if (beneficiario == null) {
          throw new RecursoNoEncontradoException("No existe un beneficiario asociado a este usuario");
        }

        NecesidadRequest requestDto = new NecesidadRequest(
            req.getTipo(),
            req.getDescripcion(),
            req.getUnidadMedida(),
            req.getCategoria(),
            req.getSubcategoria(),
            req.getCantidadPorPeriodo() == 0 ? null : req.getCantidadPorPeriodo(),
            req.getPeriodo().isBlank() ? null : req.getPeriodo(),
            req.getCantidadRequerida() == 0 ? null : req.getCantidadRequerida()
        );

        Subcategoria subcategoria = repoSubcategorias.buscarTodos()
            .stream()
            .filter(s -> s.getNombre().equalsIgnoreCase(requestDto.subcategoria()))
            .filter(s -> s.getCategoria().getNombre().equalsIgnoreCase(requestDto.categoria()))
            .findFirst()
            .orElseThrow(() -> new DominioException(
                "La subcategoría no pertenece a la categoría seleccionada"
            ));

        Necesidad necesidad = NecesidadMapper.aDominio(requestDto, subcategoria);
        beneficiario.agregarNecesidad(necesidad);
        repoBeneficiarios.actualizar(beneficiario);
      });
      return ok("Necesidad registrada correctamente");
    });
  }

  @Override
  public void obtenerDatosEdicionNecesidad(NecesidadIdRequest req,
                                           StreamObserver<DatosEdicionNecesidadResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Long necesidadId = req.getIdNecesidad();
      Necesidad necesidadEncontrada = null;
      Beneficiario beneficiarioEncontrado = null;

      for (Beneficiario beneficiario : repoBeneficiarios.buscarTodos()) {
        for (Necesidad necesidad : beneficiario.getNecesidades()) {
          if (necesidad.getId().equals(necesidadId)) {
            necesidadEncontrada = necesidad;
            beneficiarioEncontrado = beneficiario;
            break;
          }
        }
        if (necesidadEncontrada != null) {
          break;
        }
      }

      if (necesidadEncontrada == null) {
        return DatosEdicionNecesidadResponse.newBuilder().setEncontrada(false).build();
      }

      NecesidadEdicionMsg necesidadMsg = NecesidadEdicionMsg.newBuilder()
          .setId(necesidadEncontrada.getId())
          .setTipo(necesidadEncontrada.getTipo().name())
          .setDescripcion(necesidadEncontrada.getDescripcion() != null ? necesidadEncontrada.getDescripcion() : "")
          .setSubcategoria(aSubcategoriaMsg(necesidadEncontrada.getSubcategoria()))
          .setUnidadMedida(necesidadEncontrada.getUnidadMedida().name())
          .setFrecuencia(necesidadEncontrada.getFrecuencia() != null ? necesidadEncontrada.getFrecuencia().name() : "")
          .setCantidadRequerida(necesidadEncontrada.getCantidadRequerida() != null ? necesidadEncontrada.getCantidadRequerida() : 0)
          .build();

      DatosEdicionNecesidadResponse.Builder builder = DatosEdicionNecesidadResponse.newBuilder()
          .setEncontrada(true)
          .setNecesidad(necesidadMsg)
          .setEsExtraordinaria(necesidadEncontrada.getTipo() == TipoNecesidad.EXTRAORDINARIA)
          .setEsRecurrente(necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE)
          .setBeneficiario(BeneficiarioMsg.newBuilder()
              .setId(beneficiarioEncontrado.getId())
              .setRazonSocial(beneficiarioEncontrado.getRazonSocial())
              .setDireccion(beneficiarioEncontrado.getDireccion())
              .build());

      repoCategorias.buscarTodos().forEach(c ->
          builder.addCategorias(CategoriaMsg.newBuilder()
              .setId(c.getId())
              .setNombre(c.getNombre())
              .build())
      );

      repoSubcategorias.buscarTodos().forEach(s ->
          builder.addSubcategorias(aSubcategoriaMsg(s))
      );

      return builder.build();
    }));
  }

  @Override
  public void actualizarNecesidad(ActualizarNecesidadRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Long necesidadId = req.getIdNecesidad();
        Necesidad necesidadEncontrada = null;
        Beneficiario beneficiarioEncontrado = null;

        for (Beneficiario beneficiario : repoBeneficiarios.buscarTodos()) {
          for (Necesidad necesidad : beneficiario.getNecesidades()) {
            if (necesidadId.equals(necesidad.getId())) {
              necesidadEncontrada = necesidad;
              beneficiarioEncontrado = beneficiario;
              break;
            }
          }
          if (necesidadEncontrada != null) {
            break;
          }
        }

        if (necesidadEncontrada == null) {
          throw new RecursoNoEncontradoException("Necesidad no encontrada");
        }

        Integer cantidadRequerida = (necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE)
            ? req.getCantidadPorPeriodo()
            : req.getCantidadRequerida();

        UnidadMedida unidadMedida = UnidadMedida.valueOf(req.getUnidadMedida());

        Subcategoria subcategoria = repoSubcategorias.buscarTodos()
            .stream()
            .filter(s -> s.getNombre().equalsIgnoreCase(req.getSubcategoria()))
            .filter(s -> s.getCategoria().getNombre().equalsIgnoreCase(req.getCategoria()))
            .findFirst()
            .orElseThrow(() -> new DominioException(
                "La subcategoría no pertenece a la categoría seleccionada"
            ));

        if (necesidadEncontrada.getTipo() == TipoNecesidad.EXTRAORDINARIA) {
          necesidadEncontrada.actualizarDatosExtraordinaria(
              req.getDescripcion(),
              subcategoria,
              unidadMedida,
              cantidadRequerida
          );
        } else if (necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE) {
          Frecuencia frecuencia = Frecuencia.valueOf(req.getPeriodo());
          necesidadEncontrada.actualizarDatosRecurrente(
              req.getDescripcion(),
              subcategoria,
              unidadMedida,
              cantidadRequerida,
              frecuencia
          );
        }

        repoBeneficiarios.actualizar(beneficiarioEncontrado);
      });
      return ok("Necesidad actualizada correctamente");
    });
  }

  //DonacionBeneficiario
  @Override
  public void listarDonacionesPorBeneficiario(BeneficiarioIdRequest req,
                                              StreamObserver<ListaDonacionesBeneficiarioResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Beneficiario beneficiario = repoBeneficiarios.buscarPorId(req.getId());
      if (beneficiario == null) {
        throw new RecursoNoEncontradoException("No existe un beneficiario asociado a este usuario");
      }

      ListaDonacionesBeneficiarioResponse.Builder builder = ListaDonacionesBeneficiarioResponse.newBuilder();
      for (Donacion d : beneficiario.getDonaciones()) {
        builder.addDonaciones(aDonacionBeneficiarioMsg(d));
      }
      return builder.build();
    }));
  }

  @Override
  public void obtenerDetalleDonacionBeneficiario(DetalleDonacionBeneficiarioRequest req,
                                                 StreamObserver<DetalleDonacionBeneficiarioResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Beneficiario beneficiario = repoBeneficiarios.buscarPorId(req.getIdBeneficiario());
      if (beneficiario == null) {
        throw new RecursoNoEncontradoException("Beneficiario no encontrado");
      }

      Donacion donacion = beneficiario.getDonaciones()
          .stream()
          .filter(d -> Long.valueOf(req.getIdDonacion()).equals(d.getId()))
          .findFirst()
          .orElse(null);

      if (donacion == null) {
        throw new RecursoNoEncontradoException("Donación no encontrada para este beneficiario");
      }

      return DetalleDonacionBeneficiarioResponse.newBuilder()
          .setDonacion(aDonacionBeneficiarioMsg(donacion))
          .setFechaAsignacion(fechaAsignacionDe(donacion))
          .build();
    }));
  }

  @Override
  public void listarNovedadesBeneficiario(BeneficiarioIdRequest req,
                                          StreamObserver<ListaNovedadesBeneficiarioResponse> out) {
    responder(out, () -> withTransaction(() -> {
      if (repoBeneficiarios.buscarPorId(req.getId()) == null) {
        throw new RecursoNoEncontradoException("Beneficiario no encontrado");
      }

      List<NovedadDonacionMsg> novedades = new ArrayList<>();
      for (Donacion donacion : repoDonaciones.buscarPorBeneficiario(req.getId())) {
        for (EstadoDonacion estado : donacion.getHistorialEstados()) {
          if (estado.getTipoEstado() == TipoEstadoDonacion.EN_DEPOSITO) {
            continue;
          }
          novedades.add(NovedadDonacionMsg.newBuilder()
              .setIdDonacion(donacion.getId())
              .setDescripcion(nvl(donacion.getDescripcion()))
              .setTipoEstado(estado.getTipoEstado().name())
              .setFecha(estado.getFecha() == null ? "" : estado.getFecha().format(FECHA_HORA_ISO))
              .setDetalle(nvl(estado.getDetalle()))
              .build());
        }
      }

      // Formato ISO de ancho fijo: el orden alfabético coincide con el cronológico.
      // Más recientes primero; las que no tienen fecha ("") quedan al final.
      novedades.sort(Comparator.comparing(NovedadDonacionMsg::getFecha, Comparator.reverseOrder()));

      return ListaNovedadesBeneficiarioResponse.newBuilder()
          .addAllNovedades(novedades)
          .build();
    }));
  }

  //Asignacion
  @Override
  public void listarRankingsVigentes(Vacio req, StreamObserver<ListaRankingsResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<Ranking> rankingsVigentes = repoRankings.buscarTodos();
      ListaRankingsResponse.Builder builder = ListaRankingsResponse.newBuilder();

      RankingMapper.aDto(rankingsVigentes).forEach(rDto -> {
        DonacionRankingMsg.Builder donacionMsg = DonacionRankingMsg.newBuilder()
            .setId(rDto.donacion().id())
            .setDescripcion(rDto.donacion().descripcion() != null ? rDto.donacion().descripcion() : "")
            .setEstado(rDto.donacion().estado() != null ? rDto.donacion().estado() : "");

        if (rDto.donacion().donantes() != null) {
          rDto.donacion().donantes().forEach(donanteDto ->
              donacionMsg.addDonantes(DonanteNombreMsg.newBuilder()
                  .setNombre(donanteDto.nombre() != null ? donanteDto.nombre() : "")
                  .build())
          );
        }

        RankingVistaMsg.Builder rankingMsg = RankingVistaMsg.newBuilder()
            .setId(rDto.id())
            .setFechaGeneracion(rDto.fechaGeneracion() != null ? rDto.fechaGeneracion().toString() : "")
            .setDonacion(donacionMsg.build());

        if (rDto.candidatos() != null) {
          rDto.candidatos().forEach(cDto ->
              rankingMsg.addCandidatos(CandidatoRankingMsg.newBuilder()
                  .setPosicion(cDto.posicion())
                  .setBeneficiario(BeneficiarioMsg.newBuilder()
                      .setId(cDto.beneficiario().id())
                      .setRazonSocial(cDto.beneficiario().razonSocial() != null ? cDto.beneficiario().razonSocial() : "")
                      .setDireccion(cDto.beneficiario().direccion() != null ? cDto.beneficiario().direccion() : "")
                      .build())
                  .build())
          );
        }

        builder.addRankings(rankingMsg.build());
      });

      return builder.build();
    }));
  }

  @Override
  public void confirmarAsignacion(ConfirmarAsignacionRequest req, StreamObserver<Resultado> out) {
    responder(out, () -> {
      withTransaction(() -> {
        Ranking ranking = repoRankings.buscarPorId(req.getIdRanking());
        if (ranking == null) {
          throw new RecursoNoEncontradoException("No existe el ranking: " + req.getIdRanking());
        }
        Beneficiario beneficiarioSeleccionado = repoBeneficiarios.buscarPorId(req.getIdBeneficiario());
        if (beneficiarioSeleccionado == null) {
          throw new RecursoNoEncontradoException("No existe el beneficiario: " + req.getIdBeneficiario());
        }

        Donacion donacion = ranking.getDonacion();
        donacion.asignarA(beneficiarioSeleccionado);
        ranking.invalidar();

        repoDonaciones.actualizar(donacion);
        repoBeneficiarios.actualizar(beneficiarioSeleccionado);
        repoRankings.actualizar(ranking);
      });
      return ok("Asignación confirmada correctamente");
    });
  }

  // Importador CSV
  @Override
  public void importarDonantesCsv(ImportarCsvRequest req, StreamObserver<ImportarCsvResponse> out) {
    responder(out, () -> {
      //Guardamos el archivo recibido por gRPC en la carpeta local de uploads del microservicio
      Path directorioCsv = Path.of("microservicio-donacion", "uploads", "ex-donantes");
      Path rutaDestino;
      try {
        if (!Files.exists(directorioCsv)) {
          Files.createDirectories(directorioCsv);
        }
        String nombreUnico = UUID.randomUUID().toString().substring(0, 8) + "-" + req.getNombreArchivo();
        rutaDestino = directorioCsv.resolve(nombreUnico);
        Files.write(rutaDestino, req.getContenidoCsv().toByteArray());
      } catch (Exception e) {
        throw new RuntimeException("Error al guardar el archivo CSV en disco", e);
      }

      ImportadorCsv importador = new ImportadorCsv();
      ResultadoImportacion resultado = importador.importarDesdeArchivo(rutaDestino.toString());

      ImportarCsvResponse.Builder responseBuilder = ImportarCsvResponse.newBuilder();

      //Persistimos los donantes
      withTransaction(() -> {
        for (Donante donante : resultado.getDonantes()) {
          repoDonantes.guardar(donante);

          String username = donante.getNombreCompleto().substring(0, 3) + donante.getDocumento().getDetalle();
          String password = generarContraseniaTemporal(); //revisar

          donante.recibirNotificacion(String.format(
              "Bienvenido al nuevo sistema de DonaTrack %s. Tu cuenta ha sido creada. \n Usuario: %s \n Contraseña temporal: %s \n ",
              donante.getNombreCompleto(), username, password
          ));

          //Armamos la respuesta para que el BFF persista a los donantes en tabla de Usuarios
          responseBuilder.addUsuariosGenerados(DonanteImportadoUsuarioMsg.newBuilder()
              .setIdDonante(donante.getId())
              .setUsername(username)
              .setPassword(password)
              .build());
        }
      });

      responseBuilder.setCantExitosa(resultado.getDonantes().size());

      for (FilaError error : resultado.getRegistroFallas()) {
        responseBuilder.addErroresEncontrados(FilaErrorMsg.newBuilder()
            .setNumeroFila(error.getNumeroFila())
            .setMotivoError(error.getMotivoError())
            .build());
      }

      return responseBuilder.build();
    });
  }

  @Override
  public void listarSubcategorias(Vacio req, StreamObserver<ListaSubcategoriasResponse> out) {
    responder(out, () -> withTransaction(() -> {
      List<Subcategoria> subcategorias = repoSubcategorias.buscarTodas();
      ListaSubcategoriasResponse.Builder builder = ListaSubcategoriasResponse.newBuilder();

      for (Subcategoria s : subcategorias) {
        builder.addSubcategorias(aSubcategoriaMsg(s));
      }
      return builder.build();
    }));
  }

  //Buscador de donantes de la pantalla "Registrar entrega"
  @Override
  public void buscarDonantes(BuscarDonantesRequest req, StreamObserver<ListaDonantesResumenResponse> out) {
    responder(out, () -> withTransaction(() -> {
      String q = req.getQ().trim().toLowerCase();
      ListaDonantesResumenResponse.Builder builder = ListaDonantesResumenResponse.newBuilder();

      repoDonantes.buscarTodos().stream()
          .filter(d -> q.isEmpty()
              || nvlTexto(d.getNombreCompleto()).toLowerCase().contains(q)
              || documentoDe(d).toLowerCase().contains(q))
          .limit(20)
          .forEach(d -> builder.addDonantes(DonanteResumenMsg.newBuilder()
              .setId(d.getId())
              .setNombre(nvlTexto(d.getNombreCompleto()))
              .setDocumento(documentoDe(d))
              .setTipoPersona(nvlTexto(d.getTipo()).toUpperCase())
              .build()));

      return builder.build();
    }));
  }

  //Crea el RegistroEntrega y lo segmenta en donaciones EN_DEPOSITO
  @Override
  public void registrarEntrega(RegistrarEntregaRequest req, StreamObserver<RegistrarEntregaResponse> out) {
    responder(out, () -> withTransaction(() -> {
      if (req.getIdDonante() == 0) throw new DominioException("Elegí una persona donante");
      if (req.getBienesCount() == 0) throw new DominioException("La entrega debe tener al menos un bien");

      Donante donante = repoDonantes.buscarPorId(req.getIdDonante());
      if (donante == null) throw new DominioException("La persona donante no existe");

      List<Bien> bienes = req.getBienesList().stream().map(this::aBien).toList();
      String observaciones = req.getObservaciones().isBlank() ? null : req.getObservaciones().trim();
      RegistroEntrega registro = new RegistroEntrega(
          parsearFecha(req.getFechaRecepcion(), "fecha de recepción"), observaciones, bienes, donante);

      List<Donacion> donaciones = segmentador.segmentar(List.of(registro));
      donaciones.forEach(repoDonaciones::guardar);
      repoRegistros.guardar(registro);

      return RegistrarEntregaResponse.newBuilder()
          .setIdRegistro(registro.getId() == null ? 0 : registro.getId())
          .setCantidadDonaciones(donaciones.size())
          .build();
    }));
  }

  @Override
  public void obtenerPerfilDonante(DonanteIdRequest req, StreamObserver<PerfilDonanteResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Donante donante = repoDonantes.buscarPorId(req.getId());
      if (donante == null) {
        throw new RecursoNoEncontradoException("No existe el donante con id: " + req.getId());
      }

      PerfilDonanteResponse.Builder builder = PerfilDonanteResponse.newBuilder()
          .setTipoDocumento(donante.getDocumento().getTipoDocumento().name())
          .setNumeroDocumento(donante.getDocumento().getDetalle());

      donante.getContactos().forEach(c -> builder.addContactos(aContactoMsg(c)));
      if (donante instanceof Humana h) {
        builder.setTipoPersona("HUMANA")
            .setNombre(nvl(h.getNombre()))
            .setApellido(nvl(h.getApellido()))
            .setFechaNacimiento(h.getFechaNacimiento() == null ? "" : h.getFechaNacimiento().toString())
            .setGenero(h.getGenero() == null ? "" : h.getGenero().name())
            .setDireccion(nvl(h.getDireccion()));
      } else if (donante instanceof Juridica j) {
        builder.setTipoPersona("JURIDICA")
            .setNombre(nvl(j.getRazonSocial()))
            .setTipoOrganizacion(j.getTipoOrganizacion() == null ? "" : j.getTipoOrganizacion().name())
            .setRubro(nvl(j.getRubro()));
        j.getRepresentantes().stream().findFirst().ifPresent(r ->
            builder.setRepresentante(RepresentanteMsg.newBuilder()
                .setNombre(nvl(r.getNombre()))
                .setApellido(nvl(r.getApellido()))
                .setTipoDocumento(r.getDocumento().getTipoDocumento().name())
                .setNumeroDocumento(r.getDocumento().getDetalle())
                .setDireccion(nvl(r.getDireccion()))
                .build()));
      }

      builder.setResumen(ResumenDonacionesMapper.aMsg(
          repoDonaciones.buscarTodasPorDonante(donante.getId())));

      UltimaActividadMsg.Builder ultima = UltimaActividadMsg.newBuilder().setExiste(false);
      repoRegistros.buscarUltimoPorDonante(donante.getId()).ifPresent(r -> ultima
          .setExiste(true)
          .setFecha(r.getFecha().toString())
          .setDescripcion(nvl(r.getObservaciones()))
          .setCantidadBienes(r.getBienes().size()));
      builder.setUltimaEntrega(ultima.build());

      return builder.build();
    }));
  }

  @Override
  public void obtenerPerfilBeneficiario(BeneficiarioIdRequest req,
                                        StreamObserver<PerfilBeneficiarioResponse> out) {
    responder(out, () -> withTransaction(() -> {
      Beneficiario b = repoBeneficiarios.buscarPorId(req.getId());
      if (b == null) {
        throw new RecursoNoEncontradoException("No existe el beneficiario con id: " + req.getId());
      }

      List<Donacion> donaciones = b.getDonaciones();
      int abiertas = (int) b.getNecesidades().stream()
          .filter(n -> !n.estaSatisfecha())
          .count();

      PerfilBeneficiarioResponse.Builder builder = PerfilBeneficiarioResponse.newBuilder()
          .setRazonSocial(nvl(b.getRazonSocial()))
          .setDireccion(nvl(b.getDireccion()))
          .setResumen(ResumenDonacionesMapper.aMsg(donaciones))
          .setNecesidadesAbiertas(abiertas);

      b.getContactos().forEach(c -> builder.addContactos(aContactoMsg(c)));

      UltimaActividadMsg.Builder ultima = UltimaActividadMsg.newBuilder().setExiste(false);
      donaciones.stream()
          .filter(d -> !fechaAsignacionDe(d).isEmpty())
          .max(Comparator.comparing(this::fechaAsignacionDe))
          .ifPresent(d -> ultima.setExiste(true)
              .setFecha(fechaAsignacionDe(d))
              .setDescripcion(nvl(d.getDescripcion()))
              .setEstado(d.getEstadoActual().name()));
      builder.setUltimaDonacion(ultima.build());

      return builder.build();
    }));
  }

  //================ FUNCIONES AUXILIARES ===============

  private String generarContraseniaTemporal() {
    String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder codigo = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
    }
    return codigo.toString();
  }

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

  private List<MedioContacto> aDominioContactos(List<ContactoMsg> contactosProto) {
    List<ContactoDto> dtos = contactosProto.stream()
        .map(c -> new ContactoDto(c.getTipoContacto(), c.getValor(), c.getEsPrincipal()))
        .toList();
    return ContactoMapper.aDominio(dtos);
  }

  private BeneficiarioDetalleMsg aBeneficiarioDetalleMsg(Beneficiario b) {
    BeneficiarioDetalleMsg.Builder builder = BeneficiarioDetalleMsg.newBuilder()
        .setId(b.getId())
        .setRazonSocial(b.getRazonSocial() != null ? b.getRazonSocial() : "")
        .setDireccion(b.getDireccion() != null ? b.getDireccion() : "");

    // Mapeamos las necesidades que lee detalle_beneficiario.hbs
    if (b.getNecesidades() != null) {
      b.getNecesidades().forEach(n -> {
        NecesidadVistaMsg.Builder nMsg = NecesidadVistaMsg.newBuilder()
            .setDescripcion(n.getDescripcion() != null ? n.getDescripcion() : "")
            .setTipo(n.getTipo() != null ? n.getTipo().toString() : "")
            .setFrecuencia(n.getFrecuencia() != null ? n.getFrecuencia().toString() : "")
            .setCantidadRequerida(n.getCantidadRequerida() != null ? String.valueOf(n.getCantidadRequerida()) : "0")
            .setCantidadRecibida(n.getCantidadRecibida() != null ? String.valueOf(n.getCantidadRecibida()) : "0")
            .setUnidadMedida(n.getUnidadMedida() != null ? n.getUnidadMedida().toString() : "");

        if (n.getSubcategoria() != null) {
          nMsg.setSubcategoria(aSubcategoriaMsg(n.getSubcategoria()));
        }
        builder.addNecesidades(nMsg.build());
      });
    }
    return builder.build();
  }

  private SubcategoriaMsg aSubcategoriaMsg(Subcategoria s) {
    SubcategoriaMsg.Builder builder = SubcategoriaMsg.newBuilder()
        .setId(s.getId())
        .setNombre(s.getNombre() != null ? s.getNombre() : "");
    if (s.getCategoria() != null) {
      builder.setCategoria(CategoriaMsg.newBuilder()
          .setId(s.getCategoria().getId())
          .setNombre(s.getCategoria().getNombre() != null ? s.getCategoria().getNombre() : "")
          .build());
    }
    return builder.build();
  }

  private DonacionBeneficiarioMsg aDonacionBeneficiarioMsg(Donacion d) {
    DonacionBeneficiarioMsg.Builder builder = DonacionBeneficiarioMsg.newBuilder()
        .setId(d.getId())
        .setDescripcion(d.getDescripcion() != null ? d.getDescripcion() : "")
        .setEstadoActual(d.getEstadoActual() != null ? d.getEstadoActual().name() : "")
        .setFechaAsignacion(fechaAsignacionDe(d));

    return builder.build();
  }

  // getFechaAsignacion() lanza DominioException si la donación nunca fue asignada
  private String fechaAsignacionDe(Donacion d) {
    try {
      return d.getFechaAsignacion().toLocalDate().toString();
    } catch (DominioException e) {
      return "";
    }
  }

  private DonacionAdminFilaMsg aDonacionAdminFilaMsg(Donacion d) {
    LocalDate vence = DonacionVistaMapper.vencimientoDe(d);
    String fechaIngreso = d.getHistorialEstados().isEmpty() || d.getHistorialEstados().get(0).getFecha() == null
        ? ""
        : d.getHistorialEstados().get(0).getFecha().toLocalDate().toString();

    DonacionAdminFilaMsg.Builder msg = DonacionAdminFilaMsg.newBuilder()
        .setId(d.getId())
        .setFechaIngreso(fechaIngreso)
        .setVencimiento(vence == null ? "" : vence.toString())
        .setEstadoActual(d.getEstadoActual().name())
        .setVencible(d.sePuedeMarcarVencida());

    d.getDonantes().forEach(donante -> msg.addDonantes(nvl(donante.getNombreCompleto())));
    d.getBienes().forEach(bien -> msg.addBienes(BienFilaMsg.newBuilder()
        .setDescripcion(nvl(bien.getDescripcion()))
        .setCategoria(nvl(bien.getSubcategoria().getCategoria().getNombre()))
        .setSubcategoria(nvl(bien.getSubcategoria().getNombre()))
        .setCantidad(bien.getCantidad())
        .setUnidadMedida(bien.getUnidadMedida().toString())
        .build()));

    return msg.build();
  }

  private boolean coincideBusqueda(Donacion donacion, String q) {
    if (q.isEmpty()) return true;
    String buscado = q.toLowerCase();
    return DonacionVistaMapper.codigo(donacion.getId()).toLowerCase().contains(buscado)
        || donacion.getBienes().stream().anyMatch(b -> b.getDescripcion().toLowerCase().contains(buscado))
        || donacion.getDonantes().stream().anyMatch(d -> d.getNombreCompleto().toLowerCase().contains(buscado));
  }

  private boolean coincideCategoria(Donacion donacion, String categoria) {
    return donacion.getBienes().stream()
        .anyMatch(b -> b.getSubcategoria().getCategoria().getNombre().equalsIgnoreCase(categoria));
  }

  private boolean coincideVencimiento(Donacion donacion, String filtro, LocalDate hoy) {
    LocalDate vence = DonacionVistaMapper.vencimientoDe(donacion);
    return switch (filtro) {
      case "7_dias" -> vence != null && !vence.isAfter(hoy.plusDays(7));
      case "30_dias" -> vence != null && !vence.isAfter(hoy.plusDays(30));
      case "vencidas" -> vence != null && vence.isBefore(hoy);
      case "no_perecedero" -> vence == null;
      case "todos" -> true;
      default -> throw new DominioException("El filtro de vencimiento: " + filtro + " no existe");
    };
  }

  private String nvl(String valor) {
    return valor == null ? "" : valor;
  }

  //REGISTRO ENTREGA
  private Bien aBien(BienEntregaMsg b) {
    if (b.getDescripcion().isBlank()) throw new DominioException("Todos los bienes necesitan descripción");

    Subcategoria subcategoria = repoSubcategorias.buscarPorNombre(b.getCategoria(), b.getSubcategoria());
    if (subcategoria == null) {
      throw new DominioException("No existe la subcategoría '" + b.getSubcategoria()
          + "' en la categoría '" + b.getCategoria() + "'");
    }

    boolean perecedero = "PERECEDERO".equalsIgnoreCase(b.getTipo());
    LocalDate vencimiento = perecedero ? parsearFecha(b.getFechaVencimiento(), "fecha de vencimiento") : null;
    Boolean usado = perecedero ? null : b.getUsado();

    return BienFactory.crear(b.getTipo(), b.getDescripcion().trim(), b.getCantidad(),
        aUnidadMedida(b.getUnidadMedida()), null, subcategoria, vencimiento, usado);
  }

  private UnidadMedida aUnidadMedida(String unidad) {
    if (unidad == null || unidad.isBlank()) {
      throw new DominioException("El campo 'unidad_medida' es obligatorio, en el Bien");
    }
    try {
      return UnidadMedida.valueOf(unidad.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DominioException("La unidad de medida: " + unidad + " no existe, debe ser: "
          + Arrays.toString(UnidadMedida.values()));
    }
  }

  private LocalDate parsearFecha(String iso, String campo) {
    if (iso == null || iso.isBlank()) throw new DominioException("Falta la " + campo);
    try {
      return LocalDate.parse(iso);
    } catch (DateTimeParseException e) {
      throw new DominioException("La " + campo + " no es válida: " + iso);
    }
  }

  private String documentoDe(Donante d) {
    return d.getDocumento() == null ? "" : nvlTexto(d.getDocumento().getDetalle());
  }

  private String nvlTexto(String valor) {
    return valor == null ? "" : valor;
  }

  private ContactoMsg aContactoMsg(MedioContacto c) {
    String medio = c instanceof CorreoDeContacto ? "CORREO"
        : c instanceof SmsDeContacto ? "SMS"
          : c instanceof WhatsappDeContacto ? "WHATSAPP"
            : "";
    return ContactoMsg.newBuilder()
        .setTipoContacto(medio)
        .setValor(nvl(c.getDetalle()))
        .setEsPrincipal(Boolean.TRUE.equals(c.getEsPrincipal()))
        .build();
  }
}