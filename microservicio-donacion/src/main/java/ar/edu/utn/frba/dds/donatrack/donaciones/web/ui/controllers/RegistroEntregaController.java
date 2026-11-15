package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.vista.Opcion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.SubcategoriaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import io.javalin.http.Context;


public class RegistroEntregaController implements WithSimplePersistenceUnit {
  private final SubcategoriaRepository repoSubcategorias;

  public RegistroEntregaController(SubcategoriaRepository subcategoriaRepository) {
    this.repoSubcategorias = subcategoriaRepository;
  }

  public void mostrarFormulario(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    Map<String, List<String>> catalogo = new TreeMap<>();
    repoSubcategorias.buscarTodas().forEach(s ->
        catalogo.computeIfAbsent(s.getCategoria().getNombre(), k -> new ArrayList<>())
            .add(s.getNombre()));
    catalogo.values().forEach(Collections::sort);

    List<Map<String, Object>> categorias = catalogo.entrySet().stream()
        .map(e -> Map.<String, Object>of("nombre", e.getKey(), "subcategorias", e.getValue()))
        .toList();

    List<Opcion> unidades = Arrays.stream(UnidadMedida.values())
        .map(u -> new Opcion(u.name(), u.toString().toLowerCase()))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("hoy", LocalDate.now().toString());
    modelo.put("categorias", categorias);
    modelo.put("unidades", unidades);

    context.render("views/admin/registrar_entrega.hbs", modelo);
  }
}