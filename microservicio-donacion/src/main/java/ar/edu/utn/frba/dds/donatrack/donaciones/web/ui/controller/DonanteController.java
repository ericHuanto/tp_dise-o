package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.CategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonanteController {
  private final DonanteRepository repoDonantes;
  private final DonacionRepository repoDonaciones;
  private final CategoriaRepository repoCategorias;
  private final SubcategoriaRepository repoSubcategorias;
  private final BeneficiarioRepository repoBeneficiarios;

  public DonanteController(DonanteRepository repoDonantes, DonacionRepository repoDonaciones,
                           CategoriaRepository repoCategorias, SubcategoriaRepository repoSubcategorias,
                           BeneficiarioRepository repoBeneficiarios) {
    this.repoDonantes = repoDonantes;
    this.repoDonaciones = repoDonaciones;
    this.repoCategorias = repoCategorias;
    this.repoSubcategorias = repoSubcategorias;
    this.repoBeneficiarios = repoBeneficiarios;
  }

  public void donacionesPropias(Context context){
    Donante donante = donanteLogueado(context);
    if (donante == null) {
      return;
    }

    TipoEstadoDonacion estado;
    Long idCategoria;
    List<Long> idsSubcategorias;
    try {
      estado = leerEstado(context);
      String categoriaParam = context.queryParam("categoria");
      idCategoria = (categoriaParam == null || categoriaParam.isBlank()) ? null : Long.valueOf(categoriaParam);
      idsSubcategorias = context.queryParamsAsClass("subcategoria", Long.class).getOrDefault(List.of());
    } catch (IllegalArgumentException | ValidationException e) {
      // ?estado=CUALQUIERA o ?categoria=hola → vuelvo a la página sin filtros
      context.redirect("/donante/donaciones");
      return;
    }

    List<Donacion> donaciones = repoDonaciones.buscarPorDonante(donante, estado, idCategoria, idsSubcategorias);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("donaciones", donaciones);
    modelo.put("estados", TipoEstadoDonacion.values());
    modelo.put("categorias", repoCategorias.buscarTodos());
    modelo.put("subcategorias", repoSubcategorias.buscarTodos());
    modelo.put("estadoElegido", estado);
    modelo.put("categoriaElegida", idCategoria);
    modelo.put("subcategoriasElegidas", idsSubcategorias);

    context.render("donaciones_donante.hbs", modelo);
  }

  public void beneficiarios(Context context) {
    if (donanteLogueado(context) == null) {
      return;
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiarios", repoBeneficiarios.buscarTodos());

    context.render("beneficiarios.hbs", modelo);
  }

  public void detalleBeneficiario(Context context) {
    if (donanteLogueado(context) == null) {
      return;
    }

    Long id;
    try {
      id = context.pathParamAsClass("id", Long.class).get();
    } catch (ValidationException e) {
      // /donante/beneficiarios/hola → vuelvo al listado
      context.redirect("/donante/beneficiarios");
      return;
    }

    Beneficiario beneficiario = repoBeneficiarios.buscarPorId(id);
    if (beneficiario == null) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiario", beneficiario);

    context.render("detalle_beneficiario.hbs", modelo);
  }

  //================== FUNCIONES AUXILIARES ========================
  private Donante donanteLogueado(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.redirect("/login");
      return null;
    }
    Donante donante = repoDonantes.buscarPorUsuario(usuarioActual);
    if (donante == null) {
      context.redirect("/");
      return null;
    }
    return donante;
  }

  private TipoEstadoDonacion leerEstado(Context context) {
    String estado = context.queryParam("estado");
    if (estado == null || estado.isBlank()) {
      return null;
    }
    return TipoEstadoDonacion.valueOf(estado);
  }

}
