package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.CategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.NecesidadMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.necesidad.NecesidadRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.HashMap;
import io.javalin.http.Context;
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
    Beneficiario beneficiario = beneficiarioRepository.buscarTodos()
        .stream()
        .findFirst()
        .orElse(null);

    Map<String, Object> modelo = new HashMap<>();

    if (beneficiario != null) {
      modelo.put(
          "necesidades",
          NecesidadMapper.aDto(beneficiario.getNecesidades())
      );
    }
    context.render("necesidades.hbs", modelo);
  }

  public void mostrarFormulario(Context context) {
    Map<String, Object> modelo = new HashMap<>();

    modelo.put("categorias", categoriaRepository.buscarTodos());
    modelo.put("subcategorias", subcategoriaRepository.buscarTodos());

    System.out.println("CATEGORIAS: " + categoriaRepository.buscarTodos().size());
    System.out.println("SUBCATEGORIAS: " + subcategoriaRepository.buscarTodos().size());


    context.render("nueva_necesidad.hbs", modelo);
  }

  public void guardar(Context context) {

    beginTransaction();

    try {
      // Obtener el beneficiario (temporalmente, el primero registrado)
      Beneficiario beneficiario = beneficiarioRepository.buscarTodos()
          .stream()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              "No hay beneficiarios registrados"
          ));

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

  private Integer obtenerEntero(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    return Integer.valueOf(valor);
  }

}
