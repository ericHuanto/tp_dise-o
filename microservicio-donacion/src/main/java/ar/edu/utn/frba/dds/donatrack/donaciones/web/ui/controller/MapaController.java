package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.BeneficiarioMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioMapaResponse;
import com.google.gson.Gson;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaController {
  private BeneficiarioRepository repoBeneficiarios;

  public MapaController(BeneficiarioRepository beneficiarioRepository) {
    this.repoBeneficiarios = beneficiarioRepository;
  }

  public void mostrarDonacionesRecibidas(Context context) {
    List<Beneficiario> beneficiarios = repoBeneficiarios.buscarBeneficiariosConDonacionesRecibidas();
    List<BeneficiarioMapaResponse> beneficiariosDto = BeneficiarioMapper.aDtoMapa(beneficiarios);
    String beneficiariosJson = new Gson().toJson(beneficiariosDto);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("marcadoresJson", beneficiariosJson);

    context.render("mapa_beneficiarios.hbs", modelo);
  }

}
