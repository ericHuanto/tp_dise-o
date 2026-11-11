package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.Ranking;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.RankingRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.RankingMapper;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionController implements WithSimplePersistenceUnit {
  private RankingRepository repoRankings;
  private BeneficiarioRepository repoBeneficiarios;
  private DonacionRepository repoDonaciones;

  public AsignacionController(RankingRepository rankingRepository, BeneficiarioRepository beneficiarioRepository, DonacionRepository donacionRepository) {
    this.repoRankings = rankingRepository;
    this.repoBeneficiarios = beneficiarioRepository;
    this.repoDonaciones = donacionRepository;
  }

  public void mostrarTodos(Context context) {
    List<Ranking> rankingsVigentes = repoRankings.buscarTodos();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("rankings", RankingMapper.aDto(rankingsVigentes));

    context.render("asignar_beneficiaria.hbs", modelo);
  }

  public void confirmarAsignacion(Context context) {
    //Cosas que recibo por URL --> Path param
    Long idRanking = Long.parseLong(context.pathParam("id"));
    //Cosas que recibo por Formulario --> FormParam
    Long idBeneficiario = Long.parseLong(context.formParam("id_beneficiario"));

    Ranking ranking = repoRankings.buscarPorId(idRanking);
    Beneficiario beneficiarioSeleccionado =  repoBeneficiarios.buscarPorId(idBeneficiario);

    Donacion donacion = ranking.getDonacion();

    try {
      beginTransaction();
      donacion.asignarA(beneficiarioSeleccionado);
      ranking.invalidar();
      repoDonaciones.actualizar(donacion);
      repoBeneficiarios.actualizar(beneficiarioSeleccionado);
      repoRankings.actualizar(ranking);
      commitTransaction();
    } catch (Exception e) {
      rollbackTransaction();
      throw new RuntimeException("Error al confirmar la asignación de la donación", e);
    }

    context.redirect("/rankings");
  }

}
