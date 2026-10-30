package ar.edu.utn.frba.dds.donatrack.donaciones.web;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.SuscriptorNotificacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.controller.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.AsignacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.BeneficiarioApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonacionApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.DonanteApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.api.routes.NecesidadApiRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.HomeController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.MapaController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller.SesionController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.HomeRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.MapaRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.routes.SesionRoutes;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinHandlebars;
import static io.javalin.apibuilder.ApiBuilder.path;

public class App {

  public static final int PUERTO = 7070;

  public static void main(String[] args) {
    DatosIniciales.init();
    SuscriptorNotificacion.init();
    crearApp().start(ConfiguracionEntorno.getInstance().puertoDonaciones(PUERTO));
  }

  public static Javalin crearApp() {
    //Repositorios
    DonanteRepository donanteRepository = DonanteRepository.getInstancia();
    DonacionRepository donacionRepository = DonacionRepository.getInstancia();
    BeneficiarioRepository benificiarioRepository = BeneficiarioRepository.getInstancia();
    RankingRepository rankingRepository = RankingRepository.getInstancia();
    SubcategoriaRepository subcategoriaRepository = SubcategoriaRepository.getInstancia();
    CategoriaRepository categoriaRepository = CategoriaRepository.getInstancia();

    //Controllers
    DonanteApiController donanteApiController = new DonanteApiController(donanteRepository);
    DonacionApiController donacionApiController = new DonacionApiController(donacionRepository, donanteRepository, subcategoriaRepository, categoriaRepository);
    BeneficiarioApiController beneficiarioApiController = new BeneficiarioApiController(benificiarioRepository);
    NecesidadApiController necesidadApiController = new NecesidadApiController(benificiarioRepository, subcategoriaRepository);
    AsignacionApiController asignacionApiController = new AsignacionApiController(donacionRepository, benificiarioRepository, rankingRepository);
    HomeController homeController = new HomeController();
    SesionController sesionController = new SesionController();
    MapaController mapaController = new MapaController();

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";
      TemplateLoader loader = new ClassPathTemplateLoader("/presentacion", "");
      Handlebars motorHandlebars = new Handlebars(loader);
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
      MapaRoutes.registrar(config.routes, mapaController);

      //Registramos las excepciones
      ExceptionHandlers.registrar(config.routes);
    });

    return app;
  }

}
