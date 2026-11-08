package ar.edu.utn.frba.dds.donatrack.donaciones.web;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.SuscriptorNotificacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.AsignacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.BeneficiarioApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonanteApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.NecesidadApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.DonacionBeneficiarioController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.DonanteController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.HomeController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.MapaController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.NecesidadController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.PerfilController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.SesionController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.DonacionBeneficiarioRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.DonanteRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.HomeRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.MapaRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.NecesidadRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.PerfilRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.SesionRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.*;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinHandlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import static io.javalin.apibuilder.ApiBuilder.path;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.grpc.DonacionesGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;


public class App {

  public static final int PUERTO = 7070;
  public static final int PUERTO_GRPC = 7072;

  public static void main(String[] args) throws IOException {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    //Cargo credenciales para la db desde el .env TODO esta configuración debería estar en persistence.xml
    //setPropiedad("hibernate.connection.driver_class", config.getDbDriver());   // LOCAL: no commitear
    System.setProperty("hibernate.connection.url", config.getDbUrlDonaciones());
    System.setProperty("hibernate.connection.username", config.getDbUser());
    System.setProperty("hibernate.connection.password", config.getDbPassword());
    //setPropiedad("hibernate.dialect",                 config.getDbDialect());   // LOCAL: no commitear

    //Si te sale error por el momento puedes usar la opción: 3 del meta-inf
    DatosIniciales.init();
    SuscriptorNotificacion.init();

    Server grpc = ServerBuilder
        .forPort(config.puertoGrpcDonaciones(PUERTO_GRPC))
        .addService(new DonacionesGrpc(DonacionRepository.getInstancia()))
        .build()
        .start();
    System.out.println("[gRPC] Servidor de donaciones escuchando en el puerto " + grpc.getPort());
    Runtime.getRuntime().addShutdownHook(new Thread(grpc::shutdown));

    crearApp().start(config.puertoDonaciones(PUERTO));
  }

  private static void setPropiedad(String clave, String valor) {
    if (valor != null) {
      System.setProperty(clave, valor);
    }
  }

  public static Javalin crearApp() {
    //Repositorios
    DonanteRepository donanteRepository = DonanteRepository.getInstancia();
    DonacionRepository donacionRepository = DonacionRepository.getInstancia();
    BeneficiarioRepository beneficiarioRepository = BeneficiarioRepository.getInstancia();
    RankingRepository rankingRepository = RankingRepository.getInstancia();
    SubcategoriaRepository subcategoriaRepository = SubcategoriaRepository.getInstancia();
    CategoriaRepository categoriaRepository = CategoriaRepository.getInstancia();
    UsuarioRepository usuarioRepository = UsuarioRepository.getInstancia();

    //Controllers
    //de api (json)
    DonanteApiController donanteApiController = new DonanteApiController(donanteRepository);
    DonacionApiController donacionApiController = new DonacionApiController(donacionRepository, donanteRepository, subcategoriaRepository, categoriaRepository);
    BeneficiarioApiController beneficiarioApiController = new BeneficiarioApiController(beneficiarioRepository);
    NecesidadApiController necesidadApiController = new NecesidadApiController(beneficiarioRepository, subcategoriaRepository);
    AsignacionApiController asignacionApiController = new AsignacionApiController(donacionRepository, beneficiarioRepository, rankingRepository);
    //de UI-UX (web)
    HomeController homeController = new HomeController();
    SesionController sesionController = new SesionController(usuarioRepository);
    PerfilController perfilController = new PerfilController(donanteRepository, beneficiarioRepository, usuarioRepository);
    MapaController mapaController = new MapaController(beneficiarioRepository);
    DonanteController donanteController = new DonanteController(donanteRepository, donacionRepository, categoriaRepository, subcategoriaRepository, beneficiarioRepository);
    DonacionController donacionController = new DonacionController(donacionRepository);
    NecesidadController necesidadController = new NecesidadController( BeneficiarioRepository.getInstancia(), CategoriaRepository.getInstancia(), SubcategoriaRepository.getInstancia());
    DonacionBeneficiarioController donacionBeneficiarioController = new DonacionBeneficiarioController( BeneficiarioRepository.getInstancia(),DonacionRepository.getInstancia());

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";
      config.staticFiles.add("/presentacion", io.javalin.http.staticfiles.Location.CLASSPATH);//TODO después ver para que es ?
      TemplateLoader loader = new ClassPathTemplateLoader("/presentacion", "");
      Handlebars motorHandlebars = new Handlebars(loader);
      for (ConditionalHelpers helper : ConditionalHelpers.values()) {
        motorHandlebars.registerHelper(helper.name(), helper);
      }
      config.fileRenderer(new JavalinHandlebars(motorHandlebars));

      //Registramos las rutas
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de donaciones funcionando"));
      //rutas que devuelven json
      DonanteApiRoutes.registrar(config.routes, donanteApiController);
      DonacionApiRoutes.registrar(config.routes, donacionApiController);
      BeneficiarioApiRoutes.registrar(config.routes, beneficiarioApiController);
      NecesidadApiRoutes.registrar(config.routes, necesidadApiController);
      AsignacionApiRoutes.registrar(config.routes, asignacionApiController);
      //rutas que devuelven vistas
      HomeRoutes.registrar(config.routes, homeController);
      SesionRoutes.registrar(config.routes, sesionController);
      PerfilRoutes.registrar(config.routes, perfilController);
      MapaRoutes.registrar(config.routes, mapaController);
      DonanteRoutes.registrar(config.routes, donanteController);
      DonacionRoutes.registrar(config.routes, donacionController);

      NecesidadRoutes.registrar(config.routes, necesidadController);
      DonacionBeneficiarioRoutes.registrar(config.routes, donacionBeneficiarioController);

      //Registramos las excepciones
      ExceptionHandlers.registrar(config.routes);

      config.routes.after(ctx -> {
        var acceso = WithSimplePersistenceUnit.PER_THREAD_ENTITY_MANAGER_ACCESS;
        if (acceso.isAttached() && acceso.get().getTransaction().isActive()) {
          acceso.get().getTransaction().rollback();
        }
        WithSimplePersistenceUnit.dispose();
      });
    });

    return app;
  }

}
