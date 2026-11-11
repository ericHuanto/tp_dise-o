package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.CategoriaRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.BeneficiarioMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioMapaResponse;
import com.google.gson.Gson;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaController {
  private BeneficiarioRepository repoBeneficiarios;
  private CategoriaRepository repoCategorias;

  public MapaController(BeneficiarioRepository beneficiarioRepository, CategoriaRepository categoriaRepository) {
    this.repoBeneficiarios = beneficiarioRepository;
    this.repoCategorias = categoriaRepository;
  }

  public void mostrarDonacionesRecibidas(Context context) {
    List<Beneficiario> beneficiarios = repoBeneficiarios.buscarBeneficiariosConDonacionesRecibidas();
    List<BeneficiarioMapaResponse> beneficiariosDto = BeneficiarioMapper.aDtoMapa(beneficiarios);
    String beneficiariosJson = new Gson().toJson(beneficiariosDto);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("marcadoresJson", beneficiariosJson);
    modelo.put("categorias", repoCategorias.buscarTodos());

    context.render("mapa_beneficiarios.hbs", modelo);
  }

}
