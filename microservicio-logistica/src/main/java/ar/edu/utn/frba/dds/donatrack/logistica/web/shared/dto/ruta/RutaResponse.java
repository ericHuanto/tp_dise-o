package ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.ruta;

import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.entrega.EntregaResumenResponse;
import java.time.LocalDate;
import java.util.List;

public record RutaResponse(
    Long id,
    String patenteCamion,
    String nombreChofer,
    LocalDate fecha,
    boolean iniciada,
    List<EntregaResumenResponse> entregas
) { }