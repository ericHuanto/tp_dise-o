package ar.edu.utn.frba.dds.donatrack.usuario.web;

import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.DatosIniciales;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.usuario.web.config.GrpcClientsConfig;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.AsignacionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CamionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CsvController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionBeneficiarioController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonanteController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.EntregaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.HomeController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MapaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.NecesidadController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.PerfilController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.RegistroEntregaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.RutaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.SesionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.middleware.Middleware;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.AsignacionRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.CamionRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.CsvRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.DonacionBeneficiarioRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.DonacionRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.DonanteRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.EntregaRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.HomeRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.MapaRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.NecesidadRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.PerfilRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.RegistroEntregaRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.RutaRoutes;
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.SesionRoutes;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinHandlebars;

public class App {
  public static final int PUERTO = 8080;

  public static void main(String[] args) {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    if (System.getenv("DB_URL_BFF") != null) {
      System.setProperty("hibernate.connection.url", config.getDbUrlBff());
      System.setProperty("hibernate.connection.username", config.getDbUser());
      System.setProperty("hibernate.connection.password", config.getDbPassword());
    }

    GrpcClientsConfig grpcClients = new GrpcClientsConfig();
    Runtime.getRuntime().addShutdownHook(new Thread(grpcClients::cerrarCanales));
    DatosIniciales.init();
    crearApp(grpcClients).start(config.puertoBff(PUERTO));
  }

  public static Javalin crearApp(GrpcClientsConfig grpcClients) {
    UsuarioRepository usuarioRepository = UsuarioRepository.getInstancia();

    //DONACIONES
    HomeController homeController = new HomeController();
    SesionController sesionController = new SesionController(grpcClients.getDonacionesStub(), usuarioRepository);
    PerfilController perfilController = new PerfilController(grpcClients.getDonacionesStub(), usuarioRepository);
    MapaController mapaController = new MapaController(grpcClients.getDonacionesStub());
    DonacionController donacionController = new DonacionController(grpcClients.getDonacionesStub());
    DonanteController donanteController = new DonanteController(grpcClients.getDonacionesStub());
    NecesidadController necesidadController = new NecesidadController(grpcClients.getDonacionesStub());
    DonacionBeneficiarioController donacionBeneficiarioController = new DonacionBeneficiarioController(grpcClients.getDonacionesStub());
    AsignacionController asignacionController = new AsignacionController(grpcClients.getDonacionesStub());
    CsvController csvController = new CsvController(grpcClients.getDonacionesStub(), usuarioRepository);
    RegistroEntregaController registroEntregaController = new RegistroEntregaController(grpcClients.getDonacionesStub());

    //LOGÍSTICA
    CamionController camionController = new CamionController(grpcClients.getLogisticaStub());
    EntregaController entregaController = new EntregaController(grpcClients.getLogisticaStub());
    RutaController rutaController = new RutaController(grpcClients.getLogisticaStub());

    return Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.staticFiles.add("/presentacion", Location.CLASSPATH);

      TemplateLoader loader = new ClassPathTemplateLoader("/presentacion", "");
      Handlebars motorHandlebars = new Handlebars(loader);
      for (ConditionalHelpers helper : ConditionalHelpers.values()) {
        motorHandlebars.registerHelper(helper.name(), helper);
      }
      config.fileRenderer(new JavalinHandlebars(motorHandlebars));

      //middleware
      config.routes.beforeMatched(Middleware::verificarPermisos);

      //RUTAS
      config.routes.get("/health", ctx -> ctx.json("BFF Usuario funcionando"));
      //Rutas de gateway/bff
      HomeRoutes.registrar(config.routes, homeController);
      SesionRoutes.registrar(config.routes, sesionController);
      PerfilRoutes.registrar(config.routes, perfilController);
      MapaRoutes.registrar(config.routes, mapaController);
      //Rutas de donaciones
      DonacionRoutes.registrar(config.routes, donacionController);
      DonanteRoutes.registrar(config.routes, donanteController);
      NecesidadRoutes.registrar(config.routes, necesidadController);
      DonacionBeneficiarioRoutes.registrar(config.routes, donacionBeneficiarioController);
      AsignacionRoutes.registrar(config.routes, asignacionController);
      CsvRoutes.registrar(config.routes, csvController);
      RegistroEntregaRoutes.registrar(config.routes, registroEntregaController);
      //Rutas de logística
      CamionRoutes.registrar(config.routes, camionController);
      EntregaRoutes.registrar(config.routes, entregaController);
      RutaRoutes.registrar(config.routes, rutaController);

      //Registramos las excepciones
      ExceptionHandlers.registrar(config.routes);

    });
  }
}