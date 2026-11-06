package ar.edu.utn.frba.dds.donatrack.logistica.web;

import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.DatosIniciales;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.GpsRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.controllers.*;
import ar.edu.utn.frba.dds.donatrack.logistica.web.coordinadores.CoordinadorRuta;
import ar.edu.utn.frba.dds.donatrack.logistica.cron.ProcesoLogistica;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.planificadorexterno.ClientePlanificadorExterno;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.planificadorexterno.ClientePlanificadorExternoMock;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.microserviciosdonaciones.ConectorDonacionesApi;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.microserviciosdonaciones.ConectorDonacionesGrpc;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.microserviciosdonaciones.ConectorDonaciones;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.web.routes.*;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.EntregaRepository;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinHandlebars;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
  public static final int PUERTO = 7071;

  public static void main(String[] args) {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    //Cargo credenciales para la db desde el .env
    System.setProperty("hibernate.connection.url", config.getDbUrlLogistica());
    System.setProperty("hibernate.connection.username", config.getDbUser());
    System.setProperty("hibernate.connection.password", config.getDbPassword());

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

    //Controllers
    //para api (json)
    PlanificacionController planificacionController = new PlanificacionController(coordinadorRuta, procesoLogistica);
    RutaController rutaController = new RutaController(rutaRepository, donacionesClient);
    EntregaController entregaController = new EntregaController(entregaRepository, donacionesClient);
    CamionController camionController = new CamionController(camionRepository, gpsRepository);
    //para web (html)
    CamionControllerUI camionControllerUI = new CamionControllerUI(camionRepository, gpsRepository);

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";

      TemplateLoader loader = new ClassPathTemplateLoader("/presentacion", "");
      Handlebars motorHandlebars = new Handlebars(loader);
      for (ConditionalHelpers helper : ConditionalHelpers.values()) {
        motorHandlebars.registerHelper(helper.name(), helper);
      }
      config.fileRenderer(new JavalinHandlebars(motorHandlebars));

      //Registramos las Rutas 
      //rutas de la api
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de logística funcionando"));
      CamionRoutes.registrar(config.routes, camionController);
      RutaRoutes.registrar(config.routes, rutaController);
      EntregaRoutes.registrar(config.routes, entregaController);
      PlanificacionRoutes.registrar(config.routes, planificacionController);
      //rutas de la web
      CamionRoutesUI.registrar(config.routes, camionControllerUI);

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