package ar.edu.utn.frba.dds.donatrack.donaciones.web;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.SuscriptorNotificacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.AsignacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.BeneficiarioApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonanteApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.NecesidadApiRoutes;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.path;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.grpc.DonacionesGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;


public class App {
  public static final int PUERTO = 7070;
  public static final int PUERTO_GRPC = 7072;

  public static void main(String[] args) throws IOException {
    ConfiguracionEntorno config = ConfiguracionEntorno.getInstance();

    //TODO esta configuración debería estar en persistence.xml
    //Cargo credenciales para la db desde el .env
    //setPropiedad("hibernate.connection.driver_class", config.getDbDriver());   // LOCAL: no commitear
    System.setProperty("hibernate.connection.url", config.getDbUrlDonaciones());
    System.setProperty("hibernate.connection.username", config.getDbUser());
    System.setProperty("hibernate.connection.password", config.getDbPassword());
    //setPropiedad("hibernate.dialect",                 config.getDbDialect());   // LOCAL: no commitear
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

    //Controllers de api (json)
    DonanteApiController donanteApiController = new DonanteApiController(donanteRepository);
    DonacionApiController donacionApiController = new DonacionApiController(donacionRepository, donanteRepository, subcategoriaRepository, categoriaRepository);
    BeneficiarioApiController beneficiarioApiController = new BeneficiarioApiController(beneficiarioRepository);
    NecesidadApiController necesidadApiController = new NecesidadApiController(beneficiarioRepository, subcategoriaRepository);
    AsignacionApiController asignacionApiController = new AsignacionApiController(donacionRepository, beneficiarioRepository, rankingRepository);

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";

      //Registramos las rutas
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de donaciones funcionando"));
      //rutas que devuelven json
      DonanteApiRoutes.registrar(config.routes, donanteApiController);
      DonacionApiRoutes.registrar(config.routes, donacionApiController);
      BeneficiarioApiRoutes.registrar(config.routes, beneficiarioApiController);
      NecesidadApiRoutes.registrar(config.routes, necesidadApiController);
      AsignacionApiRoutes.registrar(config.routes, asignacionApiController);

      //Registramos las excepciones
      ExceptionHandlers.registrar(config.routes); //Revisar

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
