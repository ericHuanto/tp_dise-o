package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Frecuencia;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.TipoNecesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.CategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.NecesidadMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.necesidad.NecesidadRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.ArrayList;
import java.util.HashMap;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;

public class NecesidadController implements WithSimplePersistenceUnit {

  private final BeneficiarioRepository beneficiarioRepository;
  private final CategoriaRepository categoriaRepository;
  private final SubcategoriaRepository subcategoriaRepository;

  public NecesidadController (
      BeneficiarioRepository beneficiarioRepository,
      CategoriaRepository categoriaRepository,
      SubcategoriaRepository subcategoriaRepository
  ) {
    this.beneficiarioRepository = beneficiarioRepository;
    this.categoriaRepository = categoriaRepository;
    this.subcategoriaRepository = subcategoriaRepository;
  }


  public void listar(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario() && !usuarioActual.isAdmin()) {
      context.status(403)
          .result("No tenés permiso para consultar necesidades");
      return;
    }

    List<Necesidad> necesidades = new ArrayList<>();

    if (usuarioActual.isAdmin()) {

      // El administrador ve todas las necesidades
      List<Beneficiario> beneficiarios =
          beneficiarioRepository.buscarTodos();

      for (Beneficiario beneficiario : beneficiarios) {
        necesidades.addAll(beneficiario.getNecesidades());
      }

    } else {

      // El beneficiario ve solamente sus necesidades
      Beneficiario beneficiario =
          beneficiarioRepository.buscarPorUsuarioId(
              usuarioActual.getId()
          );

      if (beneficiario == null) {
        context.status(404)
            .result("No existe un beneficiario asociado a este usuario");
        return;
      }

      necesidades.addAll(beneficiario.getNecesidades());
    }

    Map<String, Object> modelo = new HashMap<>();

    modelo.put("usuario", usuarioActual);
    modelo.put("esAdmin", usuarioActual.isAdmin());
    modelo.put("necesidades", NecesidadMapper.aDto(necesidades));

    context.render("views/beneficiario/necesidades.hbs", modelo);
  }


  public void mostrarFormulario(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario() && !usuarioActual.isAdmin()) {
      context.status(403)
          .result("No tenés permiso para registrar necesidades");
      return;
    }

    Map<String, Object> modelo = new HashMap<>();

    modelo.put("usuario", usuarioActual);
    modelo.put("categorias", categoriaRepository.buscarTodos());
    modelo.put("subcategorias", subcategoriaRepository.buscarTodos());

    context.render("views/beneficiario/nueva_necesidad.hbs", modelo);
  }


  public void mostrarFormularioEdicion(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isAdmin()) {
      context.status(403)
          .result("No tenés permiso para editar necesidades");
      return;
    }

    Long necesidadId = Long.valueOf(context.pathParam("id"));

    Necesidad necesidadEncontrada = null;
    Beneficiario beneficiarioEncontrado = null;

    for (Beneficiario beneficiario : beneficiarioRepository.buscarTodos()) {
      for (Necesidad necesidad : beneficiario.getNecesidades()) {
        if (necesidad.getId().equals(necesidadId)) {
          necesidadEncontrada = necesidad;
          beneficiarioEncontrado = beneficiario;
          break;
        }
      }

      if (necesidadEncontrada != null) {
        break;
      }
    }

    if (necesidadEncontrada == null) {
      context.status(404).result("Necesidad no encontrada");
      return;
    }

    Map<String, Object> modelo = new HashMap<>();

    modelo.put("usuario", usuarioActual);
    modelo.put("necesidad", necesidadEncontrada);
    modelo.put("esExtraordinaria", necesidadEncontrada.getTipo() == TipoNecesidad.EXTRAORDINARIA);
    modelo.put("esRecurrente", necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE);
    modelo.put("beneficiario", beneficiarioEncontrado);
    modelo.put("categorias", categoriaRepository.buscarTodos());
    modelo.put("subcategorias", subcategoriaRepository.buscarTodos());

    context.render("views/beneficiario/editar_necesidad.hbs", modelo);
  }


  public void guardar(Context context) {

    beginTransaction();
    try {
      Usuario usuarioActual = context.sessionAttribute("usuario_actual");
      if (usuarioActual == null) {
        context.redirect("/login");
        return;
      }
      if (!usuarioActual.isBeneficiario()) {
        context.status(403).result("No tenés permiso para registrar necesidades");
        return;
      }
      Beneficiario beneficiario =
          beneficiarioRepository.buscarPorUsuarioId(usuarioActual.getId());

      if (beneficiario == null) {
        context.status(404).result("No existe un beneficiario asociado a este usuario");
        return;
      }
      // Obtener los datos del formulario
      NecesidadRequest request = new NecesidadRequest(
          context.formParam("tipo"),
          context.formParam("descripcion"),
          context.formParam("unidadMedida"),
          context.formParam("categoria"),
          context.formParam("subcategoria"),
          obtenerEntero(context.formParam("cantidadPorPeriodo")),
          context.formParam("periodo"),
          obtenerEntero(context.formParam("cantidadRequerida"))
      );

      // Buscar la subcategoría seleccionada
      Subcategoria subcategoria = subcategoriaRepository.buscarTodos()
          .stream()
          .filter(s -> s.getNombre().equalsIgnoreCase(request.subcategoria()))
          .filter(s -> s.getCategoria().getNombre()
              .equalsIgnoreCase(request.categoria()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              "La subcategoría no pertenece a la categoría seleccionada"
          ));

      // Convertir el DTO a objeto de dominio
      Necesidad necesidad = NecesidadMapper.aDominio(request, subcategoria);

      // Agregar la necesidad al beneficiario
      beneficiario.agregarNecesidad(necesidad);

      // Actualizar el beneficiario
      beneficiarioRepository.actualizar(beneficiario);

      commitTransaction();

      context.redirect("/mis-necesidades");

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }


  public void actualizar(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isAdmin()) {
      context.status(403)
          .result("No tenés permiso para editar necesidades");
      return;
    }

    Long necesidadId;
    try {
      necesidadId = Long.valueOf(context.pathParam("id"));
    } catch (NumberFormatException e) {
      context.status(400).result("ID de necesidad inválido");
      return;
    }

    beginTransaction();

    try {
      Necesidad necesidadEncontrada = null;
      Beneficiario beneficiarioEncontrado = null;

      // Buscar la necesidad y su beneficiario
      for (Beneficiario beneficiario : beneficiarioRepository.buscarTodos()) {

        for (Necesidad necesidad : beneficiario.getNecesidades()) {

          if (necesidadId.equals(necesidad.getId())) {
            necesidadEncontrada = necesidad;
            beneficiarioEncontrado = beneficiario;
            break;
          }
        }

        if (necesidadEncontrada != null) {
          break;
        }
      }

      if (necesidadEncontrada == null) {
        rollbackTransaction();
        context.status(404).result("Necesidad no encontrada");
        return;
      }

      String descripcion = context.formParam("descripcion");
      String nombreCategoria = context.formParam("categoria");
      String nombreSubcategoria = context.formParam("subcategoria");

      Integer cantidadRequerida;

      if (necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE) {
        cantidadRequerida =
            obtenerEntero(context.formParam("cantidadPorPeriodo"));
      } else {
        cantidadRequerida =
            obtenerEntero(context.formParam("cantidadRequerida"));
      }
      UnidadMedida unidadMedida = UnidadMedida.valueOf(
          context.formParam("unidadMedida")
      );

      // Validar la categoría y subcategoría seleccionadas
      Subcategoria subcategoria = subcategoriaRepository.buscarTodos()
          .stream()
          .filter(s -> s.getNombre().equalsIgnoreCase(nombreSubcategoria))
          .filter(s -> s.getCategoria().getNombre()
              .equalsIgnoreCase(nombreCategoria))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              "La subcategoría no pertenece a la categoría seleccionada"
          ));

      // Actualizar según el tipo original de necesidad
      if (necesidadEncontrada.getTipo() == TipoNecesidad.EXTRAORDINARIA) {

        necesidadEncontrada.actualizarDatosExtraordinaria(
            descripcion,
            subcategoria,
            unidadMedida,
            cantidadRequerida
        );

      } else if (necesidadEncontrada.getTipo() == TipoNecesidad.RECURRENTE) {

        Frecuencia frecuencia = Frecuencia.valueOf(
            context.formParam("periodo")
        );

        necesidadEncontrada.actualizarDatosRecurrente(
            descripcion,
            subcategoria,
            unidadMedida,
            cantidadRequerida,
            frecuencia
        );
      }

      // Persistir los cambios
      beneficiarioRepository.actualizar(beneficiarioEncontrado);

      commitTransaction();

      context.redirect("/mis-necesidades");

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }


  //================== FUNCIONES AUXILIARES ==================
  private Integer obtenerEntero(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    return Integer.valueOf(valor);
  }

}
