package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.donacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioResumenResponse;

public record DonacionResumenResponse(
    Long id,
    String descripcion,
    String estado,
    BeneficiarioResumenResponse beneficiario
) { }
