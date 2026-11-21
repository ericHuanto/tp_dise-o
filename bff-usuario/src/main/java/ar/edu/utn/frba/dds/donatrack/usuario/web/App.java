package ar.edu.utn.frba.dds.donatrack.usuario.web;

import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionPersistencia;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.DatosIniciales;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.usuario.web.config.GrpcClientsConfig;
import ar.edu.utn.frba.dds.donatrack.usuario.web.config.ManejadorErrores;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.AsignacionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CamionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.CsvController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionBeneficiarioController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonacionController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.DonanteController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.EntregaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.HomeController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MapaController;
import ar.edu.utn.frba.dds.donatrack.usuario.web.controllers.MiPerfilController;
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
import ar.edu.utn.frba.dds.donatrack.usuario.web.routes.MiPerfilRoutes;
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
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
  public static final int PUERTO = 8080;

  public static void main(String[] args) {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    //Conexión a la db desde el .env
    configurarPersistencia();

    GrpcClientsConfig grpcClients = new GrpcClientsConfig();
    Runtime.getRuntime().addShutdownHook(new Thread(grpcClients::cerrarCanales));
    DatosIniciales.init();
    crearApp(grpcClients).start(config.puertoBff(PUERTO));
  }

  public static void configurarPersistencia() {
    String urlDb = ConfiguracionEntorno.getInstance().obtenerOpcional("DB_URL_BFF");
    if (urlDb != null) {
      WithSimplePersistenceUnit.configure(p -> p.putAll(ConfiguracionPersistencia.propiedadesBaseDeDatos(urlDb)));
    } else {
      System.out.println("[AVISO] DB_URL_BFF no está definida: se usa la base del persistence.xml");
    }
  }

  public static Javalin crearApp(GrpcClientsConfig grpcClients) {
    UsuarioRepository usuarioRepository = UsuarioRepository.getInstancia();

    //DONACIONES
    HomeController homeController = new HomeController(grpcClients.getDonacionesStub());
    SesionController sesionController = new SesionController(grpcClients.getDonacionesStub(), usuarioRepository);
    PerfilController perfilController = new PerfilController(grpcClients.getDonacionesStub(), usuarioRepository);
    MapaController mapaController = new MapaController(grpcClients.getDonacionesStub());
    DonacionController donacionController = new DonacionController(grpcClients.getDonacionesStub());
    DonanteController donanteController = new DonanteController(grpcClients.getDonacionesStub(), grpcClients.getLogisticaStub());
    NecesidadController necesidadController = new NecesidadController(grpcClients.getDonacionesStub());
    DonacionBeneficiarioController donacionBeneficiarioController = new DonacionBeneficiarioController(grpcClients.getDonacionesStub());
    AsignacionController asignacionController = new AsignacionController(grpcClients.getDonacionesStub());
    CsvController csvController = new CsvController(grpcClients.getDonacionesStub(), usuarioRepository);
    RegistroEntregaController registroEntregaController = new RegistroEntregaController(grpcClients.getDonacionesStub());
    MiPerfilController miPerfilController = new MiPerfilController(grpcClients.getDonacionesStub(), usuarioRepository);

    //LOGÍSTICA
    CamionController camionController = new CamionController(grpcClients.getLogisticaStub());
    EntregaController entregaController = new EntregaController(grpcClients.getLogisticaStub(), grpcClients.getDonacionesStub());
    RutaController rutaController = new RutaController(grpcClients.getLogisticaStub(), grpcClients.getDonacionesStub());    return Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.staticFiles.add("/presentacion", Location.CLASSPATH);

      //Guarda las fotos de recepcion de entrega en /uploads
      Path directorioFotos = Path.of(System.getenv().getOrDefault("DIRECTORIO_FOTOS", "microservicio-logistica/uploads"));
      if (Files.isDirectory(directorioFotos)) {
        config.staticFiles.add(staticFiles -> {
          staticFiles.hostedPath = "/uploads";
          staticFiles.directory = directorioFotos.toAbsolutePath().toString();
          staticFiles.location = Location.EXTERNAL;
        });
      }

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
      PerfilRoutes.registrar(config.routes, perfilController);
      SesionRoutes.registrar(config.routes, sesionController);
      MapaRoutes.registrar(config.routes, mapaController);
      //Rutas de donaciones
      DonacionRoutes.registrar(config.routes, donacionController);
      DonanteRoutes.registrar(config.routes, donanteController);
      NecesidadRoutes.registrar(config.routes, necesidadController);
      DonacionBeneficiarioRoutes.registrar(config.routes, donacionBeneficiarioController);
      AsignacionRoutes.registrar(config.routes, asignacionController);
      CsvRoutes.registrar(config.routes, csvController);
      RegistroEntregaRoutes.registrar(config.routes, registroEntregaController);
      MiPerfilRoutes.registrar(config.routes, miPerfilController);

      //Rutas de logística
      CamionRoutes.registrar(config.routes, camionController);
      EntregaRoutes.registrar(config.routes, entregaController);
      RutaRoutes.registrar(config.routes, rutaController);

      //Limpia la persistencia al terminar cada request
      config.routes.after(ctx -> {
        var acceso = WithSimplePersistenceUnit.PER_THREAD_ENTITY_MANAGER_ACCESS;
        if (acceso.isAttached() && acceso.get().getTransaction().isActive()) {
          acceso.get().getTransaction().rollback();
        }
        WithSimplePersistenceUnit.dispose();
      });

      ManejadorErrores.registrar(config.routes);

    });
  }
}