package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.asignacion;

import java.time.LocalDateTime;

public record RankingResumenResponse (
    Long id,
    LocalDateTime fechaGeneracion
) { }
