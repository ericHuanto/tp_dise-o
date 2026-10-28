package ar.edu.utn.frba.dds.donatrack.donaciones.web;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.comunicaciones.SuscriptorNotificacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.*;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.AsignacionController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.BeneficiarioController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.DonacionController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.DonanteController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.controller.NecesidadController;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.AsignacionRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.BeneficiarioRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.DonacionRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.DonanteRoutes;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.routes.NecesidadRoutes;
import ar.edu.utn.frba.dds.donatrack.shared.ConfiguracionEntorno;
import ar.edu.utn.frba.dds.donatrack.shared.ExceptionHandlers;
import ar.edu.utn.frba.dds.donatrack.shared.GsonConfig;
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

    Javalin app = Javalin.create(config -> {
      config.jsonMapper(GsonConfig.jsonMapper());
      config.http.defaultContentType = "application/json";
      config.fileRenderer(new JavalinHandlebars());

      //Registramos las Rutas
      config.routes.get("/health", ctx -> ctx.json("Micro servicio de donaciones funcionando"));
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
