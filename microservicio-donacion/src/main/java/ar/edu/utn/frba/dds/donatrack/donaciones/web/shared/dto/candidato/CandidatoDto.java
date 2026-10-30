package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.candidato;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioResumenResponse;

public record CandidatoDto(
    int posicion,
    BeneficiarioResumenResponse beneficiario
) { }
