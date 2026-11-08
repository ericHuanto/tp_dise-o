package ar.edu.utn.frba.dds.donatrack.logistica.web.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Chofer;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import ar.edu.utn.frba.dds.donatrack.logistica.web.convers.ChoferMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.convers.RutaMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.dto.chofer.ChoferDto;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.microserviciosdonaciones.ConectorDonaciones;
import ar.edu.utn.frba.dds.donatrack.logistica.web.integracion.microserviciosdonaciones.ConectorDonacionesApi;
import com.google.gson.Gson;
import io.javalin.http.Context;

import ar.edu.utn.frba.dds.donatrack.shared.excepciones.RecursoNoEncontradoException;
import java.util.List;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Coordenada;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Coordenada;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.TipoEstadoEntrega;

public class RutaController {
  private final RutaRepository repoRutas;
  private final ConectorDonaciones donacionesBridge;

  public RutaController(RutaRepository repository, ConectorDonaciones donacionesClient) {
    this.repoRutas = repository;
    this.donacionesBridge = donacionesClient;
  }

  public void obtenerTodas(Context ctx) {
    List<Ruta> rutas = repoRutas.buscarTodas();

    ctx.status(200).json(RutaMapper.aDto(rutas));
  }

  public void obtener(Context ctx) {
    Long idRuta = Long.valueOf(ctx.pathParam("id"));

    Ruta ruta = buscarRutaPorId(idRuta);
    ctx.status(200).json(RutaMapper.aDto(ruta));
  }

  public void asignarChofer(Context ctx) {
    //Cosas que recibo por URL
    Long idRuta = Long.valueOf(ctx.pathParam("id"));
    //Cosas que recibo por Body
    ChoferDto choferDto = ctx.bodyAsClass(ChoferDto.class);

    Chofer chofer = ChoferMapper.aDominio(choferDto);
    Ruta ruta = buscarRutaPorId(idRuta);

    ruta.asignarChofer(chofer);
    repoRutas.actualizar(ruta);
    ctx.status(200).json(RutaMapper.aDto(ruta));
  }

  public void iniciar(Context ctx) {
    Long idRuta = Long.valueOf(ctx.pathParam("id"));

    Ruta ruta = buscarRutaPorId(idRuta);
    ruta.iniciarRecorrido();
    repoRutas.actualizar(ruta);

    String linkMapa = ruta.getCamion().getLinkSeguimiento();
    List<DonacionEnTransito> donaciones = ruta.getEntregasOrdenadas().stream().flatMap(e -> e.getDonaciones().stream()).toList();
    comunicarAlasDonacionesQueEstanEnCamino(donaciones, linkMapa);

    ctx.status(200).json(RutaMapper.aDto(ruta));
  }

  //================= FUNCIONES AUXILIARES ======================
  public Ruta buscarRutaPorId(Long id) {
    Ruta ruta = repoRutas.buscarPorId(id);
    if (ruta == null) throw new RecursoNoEncontradoException("Ruta no encontrada: " + id);
    return ruta;
  }

  public void comunicarAlasDonacionesQueEstanEnCamino(List<DonacionEnTransito> donaciones, String linkMapa) {
    donaciones.forEach(d ->
        donacionesBridge.marcarDonacionEnCamino(d.getId(), linkMapa)
    );
  }


  //Todo despues cambiarlo a otra carpeta de controller
  public void verSeguimiento(Context ctx) throws Exception {
    List<Map<String, Object>> marcadores = new ArrayList<>();

    for (Ruta ruta : repoRutas.buscarTodas()) {

      if (!ruta.isEstaIniciada()) {
        continue;
      }

      boolean tieneEntregasActivas = ruta.getEntregasOrdenadas()
          .stream()
          .anyMatch(entrega ->
              entrega.getEstadoActual() == TipoEstadoEntrega.EN_TRASLADO
          );

      if (!tieneEntregasActivas) {
        continue;
      }

      Camion camion = ruta.getCamion();
      Coordenada ubicacion = camion.getUbicacionActual();

      if (ubicacion == null) {
        continue;
      }

      Map<String, Object> marcador = new HashMap<>();

      marcador.put("patente", camion.getPatente());
      marcador.put("latitud", ubicacion.getLatitud());
      marcador.put("longitud", ubicacion.getLongitud());
      marcador.put("ultimaActualizacion", "No disponible");

      marcadores.add(marcador);
    }

    String marcadoresJson = new Gson().toJson(marcadores);
    ctx.render(
        "seguimiento_entregas.hbs",
        Map.of("marcadoresJson", marcadoresJson)
    );
  }

}