package ar.edu.utn.frba.dds.donatrack.logistica.web;

import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.DatosIniciales;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.GpsRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.api.controllers.*;
import ar.edu.utn.frba.dds.donatrack.logistica.web.api.routes.*;
import ar.edu.utn.frba.dds.donatrack.logistica.web.coordinadores.CoordinadorRuta;
import ar.edu.utn.frba.dds.donatrack.logistica.cron.ProcesoLogistica;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.planificadorexterno.ClientePlanificadorExterno;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.planificadorexterno.ClientePlanificadorExternoMock;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.grpc.LogisticaGrpc;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonacionesGrpc;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.integracion.microserviciosdonaciones.ConectorDonaciones;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.javalin.Javalin;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.javalin.http.staticfiles.Location;
import java.nio.file.Path;

public class App {
  public static final int PUERTO = 7071;
  public static final int PUERTO_GRPC = 7073;

  public static void main(String[] args) throws IOException {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    //Cargo credenciales para la db desde el .env. NO COMMITEAR COMENTADO!!!
    //System.setProperty("hibernate.connection.url", config.getDbUrlLogistica());
    //System.setProperty("hibernate.connection.username", config.getDbUser());
    //System.setProperty("hibernate.connection.password", config.getDbPassword());

    //Servidor gRPC
    Server grpc = ServerBuilder
        .forPort(config.puertoGrpcLogistica(PUERTO_GRPC))
        .addService(new LogisticaGrpc())
        .build()
        .start();
    System.out.println("[gRPC] Servidor de logistica escuchando en el puerto " + grpc.getPort());
    Runtime.getRuntime().addShutdownHook(new Thread(grpc::shutdown));

    AppLogistica resultado = crearApp();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    long minutosHasta = calcularMinutosHasta(LocalTime.of(2, 0));

    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            resultado.procesoLogistica().ejecutar();
          } catch (Exception e) {
            System.err.println("[Cron] Error en planificación: " + e.getMessage());
          }
        },
        minutosHasta,
        24 * 60,
        TimeUnit.MINUTES
    );

    System.out.println("[Cron] Planificación programada en " + minutosHasta + " minutos");
    DatosIniciales.init();
    resultado.app().start(config.puertoLogistica(PUERTO));
  }

  public record AppLogistica(Javalin app, ProcesoLogistica procesoLogistica) {}

  public static AppLogistica crearApp() {
    //Servicios Externos
    //ConectorDonacionesApi donacionesClient = new ConectorDonacionesApi();
    ConectorDonaciones donacionesClient = new ConectorDonacionesGrpc();
    ClientePlanificadorExterno clienteExterno = new ClientePlanificadorExternoMock();

    //Repositorios
    EntregaRepository entregaRepository = EntregaRepository.getInstancia();
    CamionRepository camionRepository = CamionRepository.getInstancia();
    RutaRepository rutaRepository = RutaRepository.getInstancia();
    GpsRepository gpsRepository = GpsRepository.getInstancia();
    BeneficiarioRepository beneficiarioRepository = BeneficiarioRepository.getInstancia();

    //Coordinadores y procesos
    CoordinadorRuta coordinadorRuta = new CoordinadorRuta(
        rutaRepository, camionRepository, entregaRepository, beneficiarioRepository,
        donacionesClient, clienteExterno
    );
    ProcesoLogistica procesoLogistica = new ProcesoLogistica(coordinadorRuta);

    //Controllers para api (json)
    PlanificacionApiController planificacionApiController = new PlanificacionApiController(coordinadorRuta, procesoLogistica);
    RutaApiController rutaApiController = new RutaApiController(rutaRepository, donacionesClient);
    EntregaApiController entregaApiController = new EntregaApiController(entregaRepository, donacionesClient);
    CamionApiController camionApiController = new CamionApiController(camionRepository, gpsRepository);

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";
      config.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/uploads";
        staticFiles.directory = Path.of("microservicio-logistica", "uploads").toAbsolutePath().toString();
        staticFiles.location = Location.EXTERNAL;
      });

      //Rutas de la api
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de logística funcionando"));
      CamionApiRoutes.registrar(config.routes, camionApiController);
      RutaApiRoutes.registrar(config.routes, rutaApiController);
      EntregaApiRoutes.registrar(config.routes, entregaApiController);
      PlanificacionApiRoutes.registrar(config.routes, planificacionApiController);

      // Registramos las excepciones usando config.routes
      ExceptionHandlers.registrar(config.routes);

      config.routes.after(ctx -> {
        var acceso = WithLogisticaPersistenceUnit.PER_THREAD_ENTITY_MANAGER_ACCESS;
        if (acceso.isAttached() && acceso.get().getTransaction().isActive()) {
          acceso.get().getTransaction().rollback();
        }
        WithLogisticaPersistenceUnit.dispose();
      });
    });

    return new AppLogistica(app, procesoLogistica);
  }

  //============= FUNCIONES AUXILIARES ====================
  private static long calcularMinutosHasta(LocalTime hora) {
    long minutos = ChronoUnit.MINUTES.between(LocalTime.now(), hora);
    return minutos < 0 ? minutos + (24 * 60) : minutos;
  }

}