package ar.edu.utn.frba.dds.donatrack.donaciones.web;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.SuscriptorNotificacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.AsignacionRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.BeneficiarioRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.DonacionRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.DonanteRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.NecesidadRoutes;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinHandlebars;

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
    DonanteController donanteController = new DonanteController(donanteRepository);
    DonacionController donacionController = new DonacionController(donacionRepository, donanteRepository, subcategoriaRepository, categoriaRepository);
    BeneficiarioController beneficiarioController = new BeneficiarioController(benificiarioRepository);
    NecesidadController necesidadController = new NecesidadController(benificiarioRepository, subcategoriaRepository);
    AsignacionController asignacionController = new AsignacionController(donacionRepository, benificiarioRepository, rankingRepository);
    HomeController homeController = new HomeController();
    SesionController sesionController = new SesionController();
    MapaController mapaController = new MapaController();

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";
      TemplateLoader loader = new ClassPathTemplateLoader("/presentacion", "");
      Handlebars motorHandlebars = new Handlebars(loader);
      config.fileRenderer(new JavalinHandlebars(motorHandlebars));

      //Registramos las Rutas
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de donaciones funcionando"));
      config.routes.get("/", homeController::index);
      config.routes.get("/registro", sesionController::iniciarRegistro);
      config.routes.get("/login", sesionController::iniciarSesion);
      config.routes.get("/entregas/galeria", mapaController::mostrarDonacionesEntregadas);

      DonanteRoutes.registrar(config.routes, donanteController);
      DonacionRoutes.registrar(config.routes, donacionController);
      BeneficiarioRoutes.registrar(config.routes, beneficiarioController);
      NecesidadRoutes.registrar(config.routes, necesidadController);
      AsignacionRoutes.registrar(config.routes, asignacionController);

      //Registramos las excepciones
      ExceptionHandlers.registrar(config.routes);
    });

    return app;
  }

}
