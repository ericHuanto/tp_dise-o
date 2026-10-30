package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.asignacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.candidato.CandidatoDto;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.donacion.DonacionResponse;
import java.time.LocalDateTime;
import java.util.List;

public record RankingResponse(
    Long id,
    LocalDateTime fechaGeneracion,
    DonacionResponse donacion,
    List<CandidatoDto> candidatos
) { }
