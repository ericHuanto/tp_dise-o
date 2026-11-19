package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.vista;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.grpc.ResumenDonacionesMsg;
import java.util.List;

public final class ResumenDonacionesMapper {
  private ResumenDonacionesMapper() { }

  public static ResumenDonacionesMsg aMsg(List<Donacion> donaciones) {
    int entregadas = 0, enCurso = 0, fallidas = 0, vencidas = 0;
    for (Donacion d : donaciones) {
      switch (d.getEstadoActual()) {
        case ENTREGADA -> entregadas++;
        case ENTREGA_FALLIDA -> fallidas++;
        case VENCIDA -> vencidas++;
        default -> enCurso++;
      }
    }
    return ResumenDonacionesMsg.newBuilder()
        .setTotal(donaciones.size())
        .setEntregadas(entregadas).setEnCurso(enCurso)
        .setFallidas(fallidas).setVencidas(vencidas)
        .build();
  }
}