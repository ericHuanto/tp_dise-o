package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
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

  public DonanteController(DonanteRepository repoDonantes, DonacionRepository repoDonaciones,
                           CategoriaRepository repoCategorias, SubcategoriaRepository repoSubcategorias) {
    this.repoDonantes = repoDonantes;
    this.repoDonaciones = repoDonaciones;
    this.repoCategorias = repoCategorias;
    this.repoSubcategorias = repoSubcategorias;
  }
  public void donacionesPropias(Context context){

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    Donante donante = repoDonantes.buscarPorUsuario(usuarioActual);
    if (donante == null) {
      context.redirect("/");
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
    modelo.put("usuario", usuarioActual);
    modelo.put("donaciones", donaciones);
    modelo.put("estados", TipoEstadoDonacion.values());
    modelo.put("categorias", repoCategorias.buscarTodas());
    modelo.put("subcategorias", repoSubcategorias.buscarTodas());
    modelo.put("estadoElegido", estado);
    modelo.put("categoriaElegida", idCategoria);
    modelo.put("subcategoriasElegidas", idsSubcategorias);

    context.render("mis_donaciones.hbs", modelo);
  }
  private TipoEstadoDonacion leerEstado(Context context) {
    String estado = context.queryParam("estado");
    if (estado == null || estado.isBlank()) {
      return null;
    }
    return TipoEstadoDonacion.valueOf(estado);
  }

}