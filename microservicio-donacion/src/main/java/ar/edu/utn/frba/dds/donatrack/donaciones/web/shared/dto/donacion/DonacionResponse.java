package ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.donacion;

import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.beneficiario.BeneficiarioResumenResponse;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.bien.BienDto;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.donante.DonanteResumenResponse;

import java.util.List;

public record DonacionResponse(
    Long id,
    String descripcion,
    String estado,
    List<DonanteResumenResponse> donantes,
    BeneficiarioResumenResponse beneficiario,
    List<BienDto> bienes
) { }
