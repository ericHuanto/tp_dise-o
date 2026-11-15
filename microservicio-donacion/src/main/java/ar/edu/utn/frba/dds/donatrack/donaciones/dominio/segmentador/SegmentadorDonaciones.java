package ar.edu.utn.frba.dds.donatrack.donaciones.dominio.segmentador;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.Donante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.entrega.RegistroEntrega;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SegmentadorDonaciones {
  private record BienYDonante(Bien b, Donante d){}

  public List<Donacion> segmentar(List<RegistroEntrega> registros) {
    return registros.stream()
        .flatMap(registro -> registro.getBienes().stream().map(b->new BienYDonante(b, registro.getDonante())))
        .collect(Collectors.groupingBy(el->el.b.getNombreClave()))
        .values().stream()
        .map(el -> {
          List<Bien> bienes = new ArrayList<>();
          List<Donante> donantes = new ArrayList<>();
          el.forEach(byd -> {
            bienes.add(byd.b);
            if (!donantes.contains(byd.d)) donantes.add(byd.d);
          });
          return new Donacion(unirIguales(bienes), donantes);
        })
        .toList();
  }

  private List<Bien> unirIguales(List<Bien> bienes) {
    List<Bien> unidos = new ArrayList<>();
    for (Bien bien : bienes) {
      unidos.stream()
          .filter(u -> u.esMismoProductoQue(bien))
          .findFirst()
          .ifPresentOrElse(
              u -> u.sumarCantidad(bien.getCantidad()),
              () -> unidos.add(bien));
    }
    return unidos;
  }
}
